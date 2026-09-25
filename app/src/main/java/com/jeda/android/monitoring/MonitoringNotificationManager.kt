package com.jeda.android.monitoring

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.jeda.android.MainActivity
import com.jeda.android.R

class MonitoringNotificationManager(private val context: Context) {
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "Status monitoring JEDA",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Menampilkan status monitoring JEDA secara terus-menerus."
                    setShowBadge(false)
                }
            )
        }
    }

    fun show(enabled: Boolean, state: MonitoringState, packageName: String?) {
        if (!enabled) {
            cancel()
            return
        }

        val content = NotificationContentFormatter.format(enabled, state, packageName)
        val openIntent = PendingIntent.getActivity(
            context,
            REQUEST_OPEN,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val stopIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_STOP,
            Intent(context, StopMonitoringReceiver::class.java).setPackage(context.packageName),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(content.title)
            .setContentText(content.text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content.text))
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(openIntent)
            .addAction(0, "Stop monitoring", stopIntent)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun cancel() {
        notificationManager.cancel(NOTIFICATION_ID)
    }

    companion object {
        const val CHANNEL_ID = "jeda_monitoring_status"
        const val NOTIFICATION_ID = 1001
        private const val REQUEST_OPEN = 1002
        private const val REQUEST_STOP = 1003
    }
}
