package com.example.plantpal.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plants")
data class Plant(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val species: String,
    val wateringFrequencyDays: Int,
    val lastWatered: Long,
    val photoUri: String?,
    val instructions: String? = null,  // Optional care instructions (max 600 characters)
    val displayOrder: Int = 0  // For custom ordering in the list
)
