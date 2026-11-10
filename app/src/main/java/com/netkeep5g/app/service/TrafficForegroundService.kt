package com.netkeep5g.app.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import com.netkeep5g.app.data.SettingsRepository
import com.netkeep5g.app.data.TrafficStateRepository
import com.netkeep5g.app.engine.TrafficEngine
import com.netkeep5g.app.model.TrafficMode
import com.netkeep5g.app.model.TrafficState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class TrafficForegroundService : Service() {

    companion object {
        const val ACTION_START = "com.netkeep5g.app.action.START"
        const val ACTION_STOP = "com.netkeep5g.app.action.STOP"
        const val ACTION_UPDATE_SPEED = "com.netkeep5g.app.action.UPDATE_SPEED"

        const val EXTRA_MODE = "extra_mode"
        const val EXTRA_SPEED_KBPS = "extra_speed_kbps"
        const val EXTRA_INTERVAL_SEC = "extra_interval_sec"
        const val EXTRA_ENDPOINT_URL = "extra_endpoint_url"

        fun startService(
            context: Context,
            mode: TrafficMode,
            speedKBps: Int,
            intervalSec: Int,
            endpointUrl: String
        ) {
            val intent = Intent(context, TrafficForegroundService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_MODE, mode.name)
                putExtra(EXTRA_SPEED_KBPS, speedKBps)
                putExtra(EXTRA_INTERVAL_SEC, intervalSec)
                putExtra(EXTRA_ENDPOINT_URL, endpointUrl)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, TrafficForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }

        fun updateSpeed(context: Context, newSpeedKBps: Int) {
            val intent = Intent(context, TrafficForegroundService::class.java).apply {
                action = ACTION_UPDATE_SPEED
                putExtra(EXTRA_SPEED_KBPS, newSpeedKBps)
            }
            context.startService(intent)
        }
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var notificationManager: TrafficNotificationManager
    private lateinit var settingsRepository: SettingsRepository
    private var wakeLock: PowerManager.WakeLock? = null
    private var trafficEngine: TrafficEngine? = null

    private var lastRecordedBytes: Long = 0L

    override fun onCreate() {
        super.onCreate()
        notificationManager = TrafficNotificationManager(this)
        settingsRepository = SettingsRepository(this)

        trafficEngine = TrafficEngine { state ->
            onStateReceived(state)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: return START_NOT_STICKY

        when (action) {
            ACTION_START -> {
                val modeStr = intent.getStringExtra(EXTRA_MODE) ?: TrafficMode.KEEP_ALIVE.name
                val mode = TrafficMode.fromName(modeStr)
                val speed = intent.getIntExtra(EXTRA_SPEED_KBPS, 5)
                val interval = intent.getIntExtra(EXTRA_INTERVAL_SEC, 30)
                val endpoint = intent.getStringExtra(EXTRA_ENDPOINT_URL) ?: ""

                startForegroundTraffic(mode, speed, interval, endpoint)
            }
            ACTION_UPDATE_SPEED -> {
                val newSpeed = intent.getIntExtra(EXTRA_SPEED_KBPS, 5)
                trafficEngine?.updateTargetSpeed(newSpeed)
            }
            ACTION_STOP -> {
                stopForegroundTraffic()
            }
        }

        return START_STICKY
    }

    private fun startForegroundTraffic(
        mode: TrafficMode,
        speedKBps: Int,
        intervalSec: Int,
        endpoint: String
    ) {
        acquireWakeLock()

        val initialNotification = notificationManager.buildNotification(
            TrafficState(
                isRunning = true,
                mode = mode,
                targetSpeedKBps = speedKBps,
                statusMessage = "Starting traffic..."
            )
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                TrafficNotificationManager.NOTIFICATION_ID,
                initialNotification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(TrafficNotificationManager.NOTIFICATION_ID, initialNotification)
        }

        lastRecordedBytes = 0L
        trafficEngine?.start(
            scope = serviceScope,
            mode = mode,
            targetSpeedKBps = speedKBps,
            keepAliveIntervalSec = intervalSec,
            endpointUrl = endpoint
        )
    }

    private fun stopForegroundTraffic() {
        trafficEngine?.stop()
        releaseWakeLock()

        // Sync cumulative bytes
        val total = TrafficStateRepository.trafficState.value.totalBytesDownloaded
        val diff = total - lastRecordedBytes
        if (diff > 0) {
            serviceScope.launch {
                settingsRepository.addCumulativeBytes(diff)
            }
        }

        TrafficStateRepository.updateRunning(false)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
    }

    private fun onStateReceived(state: TrafficState) {
        TrafficStateRepository.updateState(state)
        notificationManager.updateNotification(state)

        // Periodically sync delta bytes to persistent cumulative storage
        val delta = state.totalBytesDownloaded - lastRecordedBytes
        if (delta >= 1_000_000L) { // every ~1MB
            lastRecordedBytes = state.totalBytesDownloaded
            serviceScope.launch {
                settingsRepository.addCumulativeBytes(delta)
            }
        }
    }

    private fun acquireWakeLock() {
        if (wakeLock == null) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "NetKeep5G:TrafficWakeLock"
            ).apply {
                setReferenceCounted(false)
            }
        }
        try {
            if (wakeLock?.isHeld == false) {
                wakeLock?.acquire(24 * 60 * 60 * 1000L) // Safe 24-hour max safety timeout
            }
        } catch (_: Exception) {}
    }

    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (_: Exception) {}
    }

    override fun onDestroy() {
        trafficEngine?.stop()
        releaseWakeLock()
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
