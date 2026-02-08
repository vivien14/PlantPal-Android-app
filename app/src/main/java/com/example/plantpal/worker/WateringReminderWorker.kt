package com.example.plantpal.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.plantpal.data.database.PlantDatabase
import com.example.plantpal.notification.NotificationHelper
import kotlinx.coroutines.flow.first

class WateringReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val plantDao = PlantDatabase.getDatabase(applicationContext).plantDao()
            val currentTime = System.currentTimeMillis()

            // Get all plants and filter those that need watering
            val allPlants = plantDao.getAllPlantsOrdered().first()
            val plantsNeedingWater = allPlants.filter { plant ->
                val daysSinceWatering = (currentTime - plant.lastWatered) / 86400000L
                daysSinceWatering >= plant.wateringFrequencyDays
            }

            // Send notification for each plant
            plantsNeedingWater.forEach { plant ->
                NotificationHelper.showWateringNotification(
                    context = applicationContext,
                    plantId = plant.id,
                    plantName = plant.name,
                    notificationId = plant.id.toInt()
                )
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "watering_reminder_work"
    }
}
