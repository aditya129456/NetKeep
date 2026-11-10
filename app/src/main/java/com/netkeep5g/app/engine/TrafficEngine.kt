package com.netkeep5g.app.engine

import com.netkeep5g.app.model.TrafficMode
import com.netkeep5g.app.model.TrafficState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.InputStream
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class TrafficEngine(
    private val onStateUpdated: (TrafficState) -> Unit
) {
    companion object {
        const val DEFAULT_CONTINUOUS_URL = "https://speed.cloudflare.com/__down?bytes=50000000"
        const val DEFAULT_KEEP_ALIVE_URL = "https://connectivitycheck.gstatic.com/generate_204"
        private const val CHUNK_SIZE = 1024 // 1 KB chunk
    }

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private val statsTracker = TrafficStatsTracker()
    private val isRunningFlag = AtomicBoolean(false)
    private var engineJob: Job? = null
    private var tickerJob: Job? = null

    private var currentMode: TrafficMode = TrafficMode.KEEP_ALIVE
    private var currentTargetSpeedKBps: Int = 5
    private var currentKeepAliveIntervalSec: Int = 30
    private var currentEndpoint: String = ""

    private val limiter = BandwidthLimiter(5)

    fun isRunning(): Boolean = isRunningFlag.get()

    fun start(
        scope: CoroutineScope,
        mode: TrafficMode,
        targetSpeedKBps: Int,
        keepAliveIntervalSec: Int,
        endpointUrl: String
    ) {
        if (isRunningFlag.getAndSet(true)) {
            // Already running
            return
        }

        currentMode = mode
        currentTargetSpeedKBps = targetSpeedKBps.coerceIn(1, 50)
        currentKeepAliveIntervalSec = keepAliveIntervalSec.coerceIn(5, 600)
        currentEndpoint = endpointUrl.ifBlank {
            if (mode == TrafficMode.CONTINUOUS) DEFAULT_CONTINUOUS_URL else DEFAULT_KEEP_ALIVE_URL
        }

        limiter.updateRate(currentTargetSpeedKBps)
        statsTracker.startSession()

        // Start periodic status ticker (every 500ms) to update UI & notification
        tickerJob = scope.launch(Dispatchers.Default) {
            while (isActive && isRunningFlag.get()) {
                publishState(status = if (currentMode == TrafficMode.CONTINUOUS) "Downloading" else "Active")
                delay(500)
            }
        }

        // Start worker job
        engineJob = scope.launch(Dispatchers.IO) {
            try {
                when (currentMode) {
                    TrafficMode.CONTINUOUS -> runContinuousTraffic()
                    TrafficMode.KEEP_ALIVE -> runKeepAliveTraffic()
                }
            } finally {
                withContext(Dispatchers.Default) {
                    publishState(status = "Stopped")
                }
            }
        }
    }

    fun updateTargetSpeed(newSpeedKBps: Int) {
        currentTargetSpeedKBps = newSpeedKBps.coerceIn(1, 50)
        limiter.updateRate(currentTargetSpeedKBps)
        publishState(status = if (isRunning()) "Speed updated to ${currentTargetSpeedKBps} KB/s" else "Idle")
    }

    fun stop() {
        if (!isRunningFlag.getAndSet(false)) return

        tickerJob?.cancel()
        engineJob?.cancel()
        tickerJob = null
        engineJob = null

        publishState(status = "Idle")
    }

    private suspend fun runContinuousTraffic() {
        val buffer = ByteArray(CHUNK_SIZE)
        var backoffMs = 1000L

        while (isRunningFlag.get()) {
            var response: Response? = null
            var stream: InputStream? = null
            try {
                val request = Request.Builder()
                    .url(currentEndpoint)
                    .addHeader("User-Agent", "NetKeep5G/1.0 (Android Native Traffic Engine)")
                    .addHeader("Cache-Control", "no-cache, no-store")
                    .build()

                response = httpClient.newCall(request).execute()

                if (!response.isSuccessful) {
                    statsTracker.recordFailure()
                    publishState(status = "HTTP error ${response.code}", error = "HTTP ${response.code}")
                    delay(backoffMs)
                    backoffMs = (backoffMs * 2).coerceAtMost(10000L)
                    continue
                }

                // Count response header bytes
                val headersLength = response.headers.byteCount()
                statsTracker.addBytes(headersLength.toInt().coerceAtLeast(100))
                statsTracker.recordSuccess()

                // Reset backoff upon successful connection
                backoffMs = 1000L

                val body = response.body ?: continue
                stream = body.byteStream()

                // Read chunks, throttle and discard immediately to maintain low memory
                while (isRunningFlag.get()) {
                    val read = stream.read(buffer)
                    if (read == -1) {
                        // End of file/stream reached, reconnect automatically
                        break
                    }
                    if (read > 0) {
                        statsTracker.addBytes(read)
                        limiter.throttle(read)
                    }
                }
            } catch (e: Exception) {
                if (!isRunningFlag.get()) break
                statsTracker.recordFailure()
                val errMsg = e.localizedMessage ?: e.javaClass.simpleName
                publishState(status = "Retrying: $errMsg", error = errMsg)
                delay(backoffMs)
                backoffMs = (backoffMs * 2).coerceAtMost(10000L)
            } finally {
                try {
                    stream?.close()
                } catch (_: Exception) {}
                try {
                    response?.close()
                } catch (_: Exception) {}
            }
        }
    }

    private suspend fun runKeepAliveTraffic() {
        var backoffMs = 1000L

        while (isRunningFlag.get()) {
            var response: Response? = null
            try {
                val request = Request.Builder()
                    .url(currentEndpoint)
                    .addHeader("User-Agent", "NetKeep5G/1.0 (Android KeepAlive)")
                    .addHeader("Cache-Control", "no-cache, no-store")
                    .build()

                response = httpClient.newCall(request).execute()

                // Calculate received bytes (headers + response body)
                var receivedBytes = response.headers.byteCount().toInt()
                val body = response.body
                if (body != null) {
                    val bodyBytes = body.bytes() // Keep-alive responses are tiny (e.g. 0-100 bytes)
                    receivedBytes += bodyBytes.size
                }

                statsTracker.addBytes(receivedBytes.coerceAtLeast(50))

                if (response.isSuccessful || response.code == 204) {
                    statsTracker.recordSuccess()
                    backoffMs = 1000L
                    publishState(status = "Keep-Alive ping sent (${receivedBytes} B)")
                } else {
                    statsTracker.recordFailure()
                    publishState(status = "Ping failed: HTTP ${response.code}", error = "HTTP ${response.code}")
                }
            } catch (e: Exception) {
                if (!isRunningFlag.get()) break
                statsTracker.recordFailure()
                val errMsg = e.localizedMessage ?: e.javaClass.simpleName
                publishState(status = "Ping error: $errMsg", error = errMsg)
            } finally {
                try {
                    response?.close()
                } catch (_: Exception) {}
            }

            // Sleep for the configured interval while observing cancellation
            val intervalMs = currentKeepAliveIntervalSec * 1000L
            val stepMs = 500L
            var waitedMs = 0L
            while (waitedMs < intervalMs && isRunningFlag.get()) {
                delay(stepMs)
                waitedMs += stepMs
            }
        }
    }

    private fun publishState(status: String, error: String? = null) {
        val state = TrafficState(
            isRunning = isRunningFlag.get(),
            mode = currentMode,
            targetSpeedKBps = currentTargetSpeedKBps,
            keepAliveIntervalSeconds = currentKeepAliveIntervalSec,
            actualSpeedKBps = statsTracker.getActualSpeedKBps(),
            totalBytesDownloaded = statsTracker.getTotalBytes(),
            successfulRequests = statsTracker.getSuccessfulRequests(),
            failedRequests = statsTracker.getFailedRequests(),
            startTimeMs = statsTracker.getSessionStartTimeMs(),
            elapsedDurationSeconds = statsTracker.getElapsedSeconds(),
            lastError = error,
            statusMessage = status,
            activeEndpointUrl = currentEndpoint
        )
        onStateUpdated(state)
    }

    fun resetStatistics() {
        statsTracker.reset()
        publishState(status = "Statistics reset")
    }
}
