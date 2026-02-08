package com.example.plantpal.worker

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

object NotificationScheduler {

    fun scheduleWateringReminders(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(false)
            .build()

        val dailyWorkRequest = PeriodicWorkRequestBuilder<WateringReminderWorker>(
            repeatInterval = 1,
            repeatIntervalTimeUnit = TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .setInitialDelay(calculateInitialDelay(), TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WateringReminderWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            dailyWorkRequest
        )
    }

    fun scheduleImmediateCheck(context: Context) {
        val immediateWorkRequest = OneTimeWorkRequestBuilder<WateringReminderWorker>()
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "${WateringReminderWorker.WORK_NAME}_immediate",
            ExistingWorkPolicy.REPLACE,
            immediateWorkRequest
        )
    }

    fun cancelWateringReminders(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WateringReminderWorker.WORK_NAME)
    }

    private fun calculateInitialDelay(): Long {
        val currentTime = System.currentTimeMillis()
        val calendar = java.util.Calendar.getInstance().apply {
            timeInMillis = currentTime
            set(java.util.Calendar.HOUR_OF_DAY, 9) // 9 AM
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)

            // If it's already past 9 AM today, schedule for 9 AM tomorrow
            if (timeInMillis <= currentTime) {
                add(java.util.Calendar.DAY_OF_MONTH, 1)
            }
        }

        return calendar.timeInMillis - currentTime
    }
}
