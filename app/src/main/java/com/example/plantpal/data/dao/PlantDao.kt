package com.example.plantpal.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.plantpal.model.Plant
import kotlinx.coroutines.flow.Flow

@Dao
interface PlantDao {

    @Query("SELECT * FROM plants ORDER BY displayOrder ASC, name ASC")
    fun getAllPlants(): Flow<List<Plant>>

    @Query("SELECT * FROM plants WHERE id = :plantId")
    suspend fun getPlantById(plantId: Long): Plant?

    @Query("SELECT * FROM plants WHERE id = :plantId")
    fun getPlantByIdFlow(plantId: Long): Flow<Plant?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlant(plant: Plant): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlants(plants: List<Plant>)

    @Update
    suspend fun updatePlant(plant: Plant)

    @Update
    suspend fun updatePlants(plants: List<Plant>)

    @Delete
    suspend fun deletePlant(plant: Plant)

    @Query("DELETE FROM plants WHERE id = :plantId")
    suspend fun deletePlantById(plantId: Long)

    @Query("DELETE FROM plants")
    suspend fun deleteAllPlants()

    @Query("SELECT * FROM plants ORDER BY displayOrder ASC, lastWatered ASC")
    fun getAllPlantsOrdered(): Flow<List<Plant>>

    @Query("SELECT MAX(displayOrder) FROM plants")
    suspend fun getMaxDisplayOrder(): Int?
}
