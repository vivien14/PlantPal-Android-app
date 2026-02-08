package com.example.plantpal.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.plantpal.data.database.PlantDatabase
import com.example.plantpal.data.repository.PlantRepository
import com.example.plantpal.model.Plant
import com.example.plantpal.notification.NotificationHelper
import com.example.plantpal.worker.NotificationScheduler
import kotlinx.coroutines.launch
import java.io.File

class PlantViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PlantRepository
    val allPlants: LiveData<List<Plant>>
    val plantsNeedingWater: LiveData<List<Plant>>

    init {
        val plantDao = PlantDatabase.getDatabase(application).plantDao()
        repository = PlantRepository(plantDao)
        allPlants = repository.allPlants.asLiveData()
        plantsNeedingWater = repository.getPlantsNeedingWater().asLiveData(timeoutInMs = 0)

        // Create notification channel
        NotificationHelper.createNotificationChannel(application)

        // Schedule periodic watering reminders
        NotificationScheduler.scheduleWateringReminders(application)

        // Seed database with sample plants
        com.example.plantpal.data.database.DatabaseSeeder.seedDatabase(application)
    }

    fun getPlantById(plantId: Long): LiveData<Plant?> {
        return repository.getPlantById(plantId).asLiveData()
    }

    fun insertPlant(plant: Plant) = viewModelScope.launch {
        repository.insertPlant(plant)
        NotificationScheduler.scheduleImmediateCheck(getApplication())
    }

    fun insertPlants(plants: List<Plant>) = viewModelScope.launch {
        repository.insertPlants(plants)
        NotificationScheduler.scheduleImmediateCheck(getApplication())
    }

    fun updatePlant(plant: Plant) = viewModelScope.launch {
        repository.updatePlant(plant)
        NotificationScheduler.scheduleImmediateCheck(getApplication())
    }

    fun deletePlant(plant: Plant) = viewModelScope.launch {
        // Delete the associated photo file if it exists
        plant.photoUri?.let { uriString ->
            try {
                val uri = Uri.parse(uriString)
                // Check if it's a file URI (from app storage)
                if (uri.scheme == "file") {
                    val file = File(uri.path ?: return@let)
                    if (file.exists()) {
                        file.delete()
                    }
                }
            } catch (e: Exception) {
                // Ignore errors when deleting photo file
            }
        }
        repository.deletePlant(plant)
    }

    fun deletePlantById(plantId: Long) = viewModelScope.launch {
        // Get the plant first to delete its photo
        repository.getPlantByIdOnce(plantId)?.let { plant ->
            plant.photoUri?.let { uriString ->
                try {
                    val uri = Uri.parse(uriString)
                    if (uri.scheme == "file") {
                        val file = File(uri.path ?: return@let)
                        if (file.exists()) {
                            file.delete()
                        }
                    }
                } catch (e: Exception) {
                    // Ignore errors when deleting photo file
                }
            }
        }
        repository.deletePlantById(plantId)
    }

    fun deleteAllPlants() = viewModelScope.launch {
        repository.deleteAllPlants()
    }

    fun waterPlant(plantId: Long) = viewModelScope.launch {
        repository.waterPlant(plantId)
        NotificationScheduler.scheduleImmediateCheck(getApplication())
    }

    fun addPlant(
        name: String,
        species: String,
        wateringFrequencyDays: Int,
        photoUri: String? = null,
        lastWatered: Long = System.currentTimeMillis(),
        instructions: String? = null
    ) = viewModelScope.launch {
        val plant = Plant(
            name = name,
            species = species,
            wateringFrequencyDays = wateringFrequencyDays,
            lastWatered = lastWatered,
            photoUri = photoUri,
            instructions = instructions
        )
        repository.insertPlant(plant)
        NotificationScheduler.scheduleImmediateCheck(getApplication())
    }

    fun updatePlantWateringSchedule(plantId: Long, newFrequencyDays: Int) = viewModelScope.launch {
        repository.getPlantByIdOnce(plantId)?.let { plant ->
            val updatedPlant = plant.copy(wateringFrequencyDays = newFrequencyDays)
            repository.updatePlant(updatedPlant)
        }
    }

    fun movePlantUp(plant: Plant, allPlants: List<Plant>) = viewModelScope.launch {
        val currentIndex = allPlants.indexOfFirst { it.id == plant.id }
        if (currentIndex > 0) {
            val previousPlant = allPlants[currentIndex - 1]
            // Swap display orders - use index-based orders to ensure uniqueness
            val updatedCurrent = plant.copy(displayOrder = currentIndex - 1)
            val updatedPrevious = previousPlant.copy(displayOrder = currentIndex)
            repository.updatePlants(listOf(updatedCurrent, updatedPrevious))
        }
    }

    fun movePlantDown(plant: Plant, allPlants: List<Plant>) = viewModelScope.launch {
        val currentIndex = allPlants.indexOfFirst { it.id == plant.id }
        if (currentIndex < allPlants.size - 1) {
            val nextPlant = allPlants[currentIndex + 1]
            // Swap display orders - use index-based orders to ensure uniqueness
            val updatedCurrent = plant.copy(displayOrder = currentIndex + 1)
            val updatedNext = nextPlant.copy(displayOrder = currentIndex)
            repository.updatePlants(listOf(updatedCurrent, updatedNext))
        }
    }
}
