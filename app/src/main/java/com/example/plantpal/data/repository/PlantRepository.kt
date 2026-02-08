package com.example.plantpal.data.repository

import com.example.plantpal.data.dao.PlantDao
import com.example.plantpal.model.Plant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PlantRepository(private val plantDao: PlantDao) {

    val allPlants: Flow<List<Plant>> = plantDao.getAllPlants()

    fun getPlantById(plantId: Long): Flow<Plant?> {
        return plantDao.getPlantByIdFlow(plantId)
    }

    suspend fun getPlantByIdOnce(plantId: Long): Plant? {
        return plantDao.getPlantById(plantId)
    }

    suspend fun insertPlant(plant: Plant): Long {
        // Auto-assign display order for new plants
        val maxOrder = plantDao.getMaxDisplayOrder() ?: -1
        val plantWithOrder = plant.copy(displayOrder = maxOrder + 1)
        return plantDao.insertPlant(plantWithOrder)
    }

    suspend fun insertPlants(plants: List<Plant>) {
        plantDao.insertPlants(plants)
    }

    suspend fun updatePlant(plant: Plant) {
        plantDao.updatePlant(plant)
    }

    suspend fun updatePlants(plants: List<Plant>) {
        plantDao.updatePlants(plants)
    }

    suspend fun deletePlant(plant: Plant) {
        plantDao.deletePlant(plant)
    }

    suspend fun deletePlantById(plantId: Long) {
        plantDao.deletePlantById(plantId)
    }

    suspend fun deleteAllPlants() {
        plantDao.deleteAllPlants()
    }

    fun getPlantsNeedingWater(): Flow<List<Plant>> {
        return plantDao.getAllPlantsOrdered().map { plants ->
            val currentTime = System.currentTimeMillis()
            val filtered = plants.filter { plant ->
                // Check if plant was watered today
                val daysSinceWatering = (currentTime - plant.lastWatered) / 86400000L

                // Only show as needing water if it's been more than the frequency days
                val needsWater = daysSinceWatering >= plant.wateringFrequencyDays

                android.util.Log.d("PlantRepository", "Plant ${plant.name}: daysSince=$daysSinceWatering, freq=${plant.wateringFrequencyDays}, needsWater=$needsWater")
                needsWater
            }
            android.util.Log.d("PlantRepository", "Filtered ${filtered.size} plants needing water out of ${plants.size} total")
            filtered
        }
    }

    suspend fun waterPlant(plantId: Long, wateredTime: Long = System.currentTimeMillis()) {
        val plant = plantDao.getPlantById(plantId)
        plant?.let {
            val updatedPlant = it.copy(lastWatered = wateredTime)
            plantDao.updatePlant(updatedPlant)
        }
    }

    suspend fun getMaxDisplayOrder(): Int {
        return plantDao.getMaxDisplayOrder() ?: 0
    }
}
