package com.example.myapplication.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medicines")
data class Medicine(
    @PrimaryKey val id: String,
    val name: String,
    val dosage: String,
    val pillsPerDose: Int,
    val times: List<String>,
    val mealTiming: String,
    val status: String,
    val inventoryId: String?,
    val lastModified: Long
)