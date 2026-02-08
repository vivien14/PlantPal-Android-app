package com.example.plantpal

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.plantpal.data.dao.PlantDao
import com.example.plantpal.data.database.PlantDatabase
import com.example.plantpal.data.repository.PlantRepository
import com.example.plantpal.model.Plant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class WaterPlantTest {

    private lateinit var database: PlantDatabase
    private lateinit var plantDao: PlantDao
    private lateinit var repository: PlantRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            PlantDatabase::class.java
        ).allowMainThreadQueries().build()

        plantDao = database.plantDao()
        repository = PlantRepository(plantDao)
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun testWaterPlant_removesPlantFromNeedingWaterList() = runBlocking {
        // Setup: Create a plant that needs water (last watered 5 days ago, frequency 3 days)
        val fiveDaysAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(5)
        val plant = Plant(
            id = 0,
            name = "Test Plant",
            species = "Test Species",
            wateringFrequencyDays = 3,
            lastWatered = fiveDaysAgo,
            photoUri = null
        )

        // Insert the plant
        val plantId = repository.insertPlant(plant)
        println("✓ Inserted plant with ID: $plantId")

        // Verify plant needs water initially
        val plantsNeedingWaterBefore = repository.getPlantsNeedingWater().first()
        println("✓ Plants needing water before watering: ${plantsNeedingWaterBefore.size}")
        assertTrue("Plant should need water initially", plantsNeedingWaterBefore.isNotEmpty())
        assertEquals("Should have exactly 1 plant needing water", 1, plantsNeedingWaterBefore.size)
        assertEquals("The plant needing water should be our test plant", plantId, plantsNeedingWaterBefore[0].id)

        // Water the plant
        repository.waterPlant(plantId)
        println("✓ Watered plant with ID: $plantId")

        // Wait a tiny bit for Flow to propagate
        Thread.sleep(100)

        // Verify plant no longer needs water
        val plantsNeedingWaterAfter = repository.getPlantsNeedingWater().first()
        println("✓ Plants needing water after watering: ${plantsNeedingWaterAfter.size}")
        assertTrue("Plant should NOT need water after watering", plantsNeedingWaterAfter.isEmpty())

        // Verify the plant was actually updated in the database
        val updatedPlant = repository.getPlantByIdOnce(plantId)
        assertNotNull("Plant should still exist in database", updatedPlant)

        val timeSinceWatering = System.currentTimeMillis() - updatedPlant!!.lastWatered
        println("✓ Time since watering: ${timeSinceWatering}ms (should be < 1000ms)")
        assertTrue("Plant should have been watered recently (within 1 second)", timeSinceWatering < 1000)

        println("\n✅ TEST PASSED: Water button functionality is working correctly!")
    }

    @Test
    fun testWaterPlant_updatesLastWateredTimestamp() = runBlocking {
        // Create a plant
        val oldTimestamp = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)
        val plant = Plant(
            id = 0,
            name = "Thirsty Plant",
            species = "Very Thirsty",
            wateringFrequencyDays = 5,
            lastWatered = oldTimestamp,
            photoUri = null
        )

        val plantId = repository.insertPlant(plant)
        println("✓ Created plant with old timestamp: $oldTimestamp")

        // Water it
        val wateringTime = System.currentTimeMillis()
        repository.waterPlant(plantId)
        println("✓ Watered plant at: $wateringTime")

        // Verify timestamp was updated
        val updatedPlant = repository.getPlantByIdOnce(plantId)
        assertNotNull(updatedPlant)

        val timeDifference = updatedPlant!!.lastWatered - wateringTime
        println("✓ Timestamp difference: ${timeDifference}ms (should be ~0)")
        assertTrue("lastWatered should be updated to current time (within 100ms)",
            Math.abs(timeDifference) < 100)

        println("\n✅ TEST PASSED: lastWatered timestamp is correctly updated!")
    }

    @Test
    fun testGetPlantsNeedingWater_filtersCorrectly() = runBlocking {
        // Create multiple plants with different watering schedules
        val now = System.currentTimeMillis()

        // Plant 1: Needs water (watered 10 days ago, frequency 3 days)
        val plant1 = Plant(
            id = 0,
            name = "Needs Water Now",
            species = "Thirsty",
            wateringFrequencyDays = 3,
            lastWatered = now - TimeUnit.DAYS.toMillis(10),
            photoUri = null
        )

        // Plant 2: Doesn't need water (watered 1 day ago, frequency 7 days)
        val plant2 = Plant(
            id = 0,
            name = "Still Good",
            species = "Hardy",
            wateringFrequencyDays = 7,
            lastWatered = now - TimeUnit.DAYS.toMillis(1),
            photoUri = null
        )

        // Plant 3: Needs water (watered 14 days ago, frequency 7 days)
        val plant3 = Plant(
            id = 0,
            name = "Also Needs Water",
            species = "Neglected",
            wateringFrequencyDays = 7,
            lastWatered = now - TimeUnit.DAYS.toMillis(14),
            photoUri = null
        )

        repository.insertPlant(plant1)
        repository.insertPlant(plant2)
        repository.insertPlant(plant3)
        println("✓ Inserted 3 plants with different watering needs")

        // Check which plants need water
        val plantsNeedingWater = repository.getPlantsNeedingWater().first()
        println("✓ Plants needing water: ${plantsNeedingWater.size}")
        plantsNeedingWater.forEach { plant ->
            println("  - ${plant.name}: last watered ${(now - plant.lastWatered) / (1000 * 60 * 60 * 24)} days ago, frequency ${plant.wateringFrequencyDays} days")
        }

        assertEquals("Should have exactly 2 plants needing water", 2, plantsNeedingWater.size)
        assertTrue("Plant 1 should need water",
            plantsNeedingWater.any { it.name == "Needs Water Now" })
        assertTrue("Plant 3 should need water",
            plantsNeedingWater.any { it.name == "Also Needs Water" })
        assertFalse("Plant 2 should NOT need water",
            plantsNeedingWater.any { it.name == "Still Good" })

        println("\n✅ TEST PASSED: getPlantsNeedingWater filters correctly!")
    }

    @Test
    fun testWaterPlant_flowEmitsImmediately() = runBlocking {
        // Create a plant that needs water
        val plant = Plant(
            id = 0,
            name = "Test Flow",
            species = "Flow Test",
            wateringFrequencyDays = 2,
            lastWatered = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(5),
            photoUri = null
        )

        val plantId = repository.insertPlant(plant)
        println("✓ Created plant needing water")

        // Collect first value from Flow
        val beforeWatering = repository.getPlantsNeedingWater().first()
        assertEquals("Should have 1 plant before watering", 1, beforeWatering.size)
        println("✓ Flow emitted ${beforeWatering.size} plant(s) before watering")

        // Water the plant
        repository.waterPlant(plantId)
        println("✓ Watered the plant")

        // Small delay for Flow propagation
        Thread.sleep(50)

        // Collect new value from Flow
        val afterWatering = repository.getPlantsNeedingWater().first()
        assertEquals("Should have 0 plants after watering", 0, afterWatering.size)
        println("✓ Flow emitted ${afterWatering.size} plant(s) after watering")

        println("\n✅ TEST PASSED: Flow emits updated values immediately!")
    }
}
