package com.example.plantpal.data.database

import android.content.Context
import com.example.plantpal.model.Plant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

object DatabaseSeeder {

    fun seedDatabase(context: Context) {
        val database = PlantDatabase.getDatabase(context)
        val plantDao = database.plantDao()

        CoroutineScope(Dispatchers.IO).launch {
            // Check if database is already seeded
            val existingPlants = plantDao.getPlantById(1)
            if (existingPlants != null) {
                return@launch // Already seeded
            }

            val currentTime = System.currentTimeMillis()

            val samplePlants = listOf(
                Plant(
                    id = 0,
                    name = "Monstera",
                    species = "Monstera deliciosa",
                    wateringFrequencyDays = 3,
                    lastWatered = currentTime - TimeUnit.DAYS.toMillis(2), // Watered 2 days ago
                    photoUri = null
                ),
                Plant(
                    id = 0,
                    name = "Cactus",
                    species = "Echinocactus grusonii",
                    wateringFrequencyDays = 14,
                    lastWatered = currentTime - TimeUnit.DAYS.toMillis(10), // Watered 10 days ago
                    photoUri = null
                )
            )

            plantDao.insertPlants(samplePlants)
        }
    }
}
