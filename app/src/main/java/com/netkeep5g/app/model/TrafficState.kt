package com.netkeep5g.app.model

import java.util.Locale

data class TrafficState(
    val isRunning: Boolean = false,
    val mode: TrafficMode = TrafficMode.KEEP_ALIVE,
    val targetSpeedKBps: Int = 5,
    val keepAliveIntervalSeconds: Int = 30,
    val actualSpeedKBps: Double = 0.0,
    val totalBytesDownloaded: Long = 0L,
    val successfulRequests: Long = 0L,
    val failedRequests: Long = 0L,
    val startTimeMs: Long = 0L,
    val elapsedDurationSeconds: Long = 0L,
    val lastError: String? = null,
    val statusMessage: String = "Idle",
    val activeEndpointUrl: String = ""
) {
    val actualSpeedKbps: Double
        get() = actualSpeedKBps * 8.0

    val targetSpeedKbps: Int
        get() = targetSpeedKBps * 8

    val formattedTotalData: String
        get() = ConsumptionEstimate.formatBytes(totalBytesDownloaded)

    val formattedDuration: String
        get() {
            val hours = elapsedDurationSeconds / 3600
            val minutes = (elapsedDurationSeconds % 3600) / 60
            val seconds = elapsedDurationSeconds % 60
            return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
        }

    val formattedActualSpeed: String
        get() = String.format(Locale.US, "%.1f KB/s", actualSpeedKBps)

    val formattedActualSpeedWithKbps: String
        get() = String.format(Locale.US, "%.1f KB/s (%.0f kbps)", actualSpeedKBps, actualSpeedKbps)
}
