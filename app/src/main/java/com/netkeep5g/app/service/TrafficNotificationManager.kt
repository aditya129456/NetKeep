package com.netkeep5g.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.netkeep5g.app.R
import com.netkeep5g.app.model.TrafficMode
import com.netkeep5g.app.model.TrafficState
import com.netkeep5g.app.ui.MainActivity

class TrafficNotificationManager(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "netkeep_traffic_channel"
        const val NOTIFICATION_ID = 5001
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = context.getString(R.string.notification_channel_desc)
                setShowBadge(false)
                setSound(null, null)
                enableVibration(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun buildNotification(state: TrafficState): Notification {
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(context, TrafficForegroundService::class.java).apply {
            action = TrafficForegroundService.ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            context,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = when (state.mode) {
            TrafficMode.CONTINUOUS -> "NetKeep 5G — Active Download"
            TrafficMode.KEEP_ALIVE -> "NetKeep 5G — Keep-Alive Active"
        }

        val contentText = when (state.mode) {
            TrafficMode.CONTINUOUS -> {
                "Speed: ${state.formattedActualSpeedWithKbps} • Total: ${state.formattedTotalData}"
            }
            TrafficMode.KEEP_ALIVE -> {
                "Pings: ${state.successfulRequests} OK / ${state.failedRequests} fail • Data: ${state.formattedTotalData}"
            }
        }

        val subText = "Duration: ${state.formattedDuration}"

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(contentText)
            .setSubText(subText)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(openAppPendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .addAction(
                android.R.drawable.ic_media_pause,
                context.getString(R.string.action_stop),
                stopPendingIntent
            )
            .build()
    }

    fun updateNotification(state: TrafficState) {
        notificationManager.notify(NOTIFICATION_ID, buildNotification(state))
    }
}
