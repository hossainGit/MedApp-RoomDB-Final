package com.example.myapplication.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medicine_schedule")
data class MedicineSchedule(
    @PrimaryKey val id: String, // Format: "medicineId_shift_date"
    val medicineId: String,
    val date: String, // Format: "yyyy-MM-dd" (simple string)
    val shift: String, // "MORNING", "NOON", "NIGHT"
    val status: String, // "PENDING", "TAKEN", "MISSED"
    val createdAt: Long
)