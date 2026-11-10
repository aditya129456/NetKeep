package com.netkeep5g.app.engine

import java.util.concurrent.atomic.AtomicLong
import kotlin.math.max

/**
 * Thread-safe tracker for actual network statistics.
 * Computes instantaneous bandwidth (KB/s) using a rolling window of actual received bytes.
 */
class TrafficStatsTracker {

    private val totalBytes = AtomicLong(0L)
    private val successfulRequests = AtomicLong(0L)
    private val failedRequests = AtomicLong(0L)

    private var sessionStartTimeMs: Long = 0L

    // Sliding window for speed measurement
    private var windowStartNs: Long = System.nanoTime()
    private var windowBytes: Long = 0L
    @Volatile
    private var smoothedSpeedKBps: Double = 0.0

    private val speedLock = Any()

    fun startSession() {
        sessionStartTimeMs = System.currentTimeMillis()
        totalBytes.set(0L)
        successfulRequests.set(0L)
        failedRequests.set(0L)
        synchronized(speedLock) {
            windowStartNs = System.nanoTime()
            windowBytes = 0L
            smoothedSpeedKBps = 0.0
        }
    }

    fun addBytes(bytes: Int) {
        if (bytes <= 0) return
        totalBytes.addAndGet(bytes.toLong())

        synchronized(speedLock) {
            windowBytes += bytes
            val nowNs = System.nanoTime()
            val elapsedNs = nowNs - windowStartNs

            // Update instantaneous speed at least every 500ms
            if (elapsedNs >= 500_000_000L) {
                val elapsedSeconds = elapsedNs.toDouble() / 1_000_000_000.0
                val instantKBps = (windowBytes / elapsedSeconds) / 1000.0

                // Exponential smoothing (alpha = 0.4) for stable readout
                smoothedSpeedKBps = if (smoothedSpeedKBps == 0.0) {
                    instantKBps
                } else {
                    (0.4 * instantKBps) + (0.6 * smoothedSpeedKBps)
                }

                windowStartNs = nowNs
                windowBytes = 0L
            }
        }
    }

    fun recordSuccess() {
        successfulRequests.incrementAndGet()
    }

    fun recordFailure() {
        failedRequests.incrementAndGet()
    }

    fun getTotalBytes(): Long = totalBytes.get()

    fun getSuccessfulRequests(): Long = successfulRequests.get()

    fun getFailedRequests(): Long = failedRequests.get()

    fun getActualSpeedKBps(): Double {
        synchronized(speedLock) {
            val nowNs = System.nanoTime()
            val elapsedNs = nowNs - windowStartNs
            // If more than 2 seconds passed without traffic, speed drops to 0
            if (elapsedNs > 2_000_000_000L && windowBytes == 0L) {
                smoothedSpeedKBps = 0.0
            }
            return smoothedSpeedKBps
        }
    }

    fun getSessionStartTimeMs(): Long = sessionStartTimeMs

    fun getElapsedSeconds(): Long {
        if (sessionStartTimeMs == 0L) return 0L
        return max(0L, (System.currentTimeMillis() - sessionStartTimeMs) / 1000L)
    }

    fun reset() {
        totalBytes.set(0L)
        successfulRequests.set(0L)
        failedRequests.set(0L)
        sessionStartTimeMs = 0L
        synchronized(speedLock) {
            windowStartNs = System.nanoTime()
            windowBytes = 0L
            smoothedSpeedKBps = 0.0
        }
    }
}
