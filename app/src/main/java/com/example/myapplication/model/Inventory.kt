package com.example.myapplication.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventory")
data class Inventory(
    @PrimaryKey val id: String,
    val name: String,
    val unit: String,
    val stock: Int,
    val lastModified: Long
)