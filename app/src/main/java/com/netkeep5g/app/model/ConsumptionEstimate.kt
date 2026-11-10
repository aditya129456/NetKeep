package com.netkeep5g.app.model

import java.util.Locale

data class ConsumptionEstimate(
    val speedKBps: Int,
    val hourlyBytes: Long,
    val dailyBytes: Long,
    val monthlyBytes: Long
) {
    companion object {
        private const val SECONDS_PER_HOUR = 3600L
        private const val SECONDS_PER_DAY = 86400L
        private const val SECONDS_PER_MONTH = 86400L * 30L

        fun calculate(speedKBps: Int): ConsumptionEstimate {
            // Speed in KB/s where 1 KB = 1,000 bytes (standard telecom / data plan counting)
            val bytesPerSecond = speedKBps.coerceAtLeast(1).toLong() * 1000L
            return ConsumptionEstimate(
                speedKBps = speedKBps,
                hourlyBytes = bytesPerSecond * SECONDS_PER_HOUR,
                dailyBytes = bytesPerSecond * SECONDS_PER_DAY,
                monthlyBytes = bytesPerSecond * SECONDS_PER_MONTH
            )
        }

        fun formatBytes(bytes: Long): String {
            return when {
                bytes < 1000L -> "$bytes B"
                bytes < 1000L * 1000L -> String.format(Locale.US, "%.1f KB", bytes / 1000.0)
                bytes < 1000L * 1000L * 1000L -> String.format(Locale.US, "%.2f MB", bytes / (1000.0 * 1000.0))
                else -> String.format(Locale.US, "%.3f GB", bytes / (1000.0 * 1000.0 * 1000.0))
            }
        }

        fun formatBinaryBytes(bytes: Long): String {
            return when {
                bytes < 1024L -> "$bytes B"
                bytes < 1024L * 1024L -> String.format(Locale.US, "%.1f KiB", bytes / 1024.0)
                bytes < 1024L * 1024L * 1024L -> String.format(Locale.US, "%.2f MiB", bytes / (1024.0 * 1024.0))
                else -> String.format(Locale.US, "%.2f GiB", bytes / (1024.0 * 1024.0 * 1024.0))
            }
        }
    }
}
