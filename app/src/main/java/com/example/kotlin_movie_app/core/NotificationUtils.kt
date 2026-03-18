package com.example.kotlin_movie_app.core

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.kotlin_movie_app.MainActivity

object NotificationUtils {
    private const val CHANNEL_ID = "movie_sync_channel"
    private const val CHANNEL_NAME = "Movie Sync Status"
    private const val NOTIFICATION_ID_SYNC = 1
    private const val NOTIFICATION_ID_OFFLINE = 2

    fun createNotificationChannel(context: Context) {
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
            description = "Movie sync status notifications"
        }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    fun showNotification(context: Context, title: String, content: String, isSuccess: Boolean = true) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )
        val icon = if (isSuccess) android.R.drawable.stat_sys_upload_done else android.R.drawable.stat_notify_error
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        try {
            with(NotificationManagerCompat.from(context)) {
                notify(if (isSuccess) NOTIFICATION_ID_SYNC else NOTIFICATION_ID_OFFLINE, builder.build())
            }
        } catch (e: SecurityException) { Log.e("NotificationUtils", "Error showing notification", e) }
    }
}