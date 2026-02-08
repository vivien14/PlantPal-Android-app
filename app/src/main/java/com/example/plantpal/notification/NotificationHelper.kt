package com.example.plantpal.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.plantpal.MainActivity
import com.example.plantpal.R

object NotificationHelper {
    const val CHANNEL_ID = "watering_reminders"
    const val CHANNEL_NAME = "Watering Reminders"
    const val CHANNEL_DESCRIPTION = "Notifications for plant watering reminders"

    const val EXTRA_PLANT_ID = "plant_id"
    const val EXTRA_PLANT_NAME = "plant_name"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                importance
            ).apply {
                description = CHANNEL_DESCRIPTION
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showWateringNotification(
        context: Context,
        plantId: Long,
        plantName: String,
        notificationId: Int
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(EXTRA_PLANT_ID, plantId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            plantId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Time to water $plantName!")
            .setContentText("Your plant needs watering today.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // For Android 13+, check notification permission
            try {
                NotificationManagerCompat.from(context).notify(notificationId, notification)
            } catch (e: SecurityException) {
                // Permission not granted, handle silently or log
            }
        } else {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        }
    }
}
