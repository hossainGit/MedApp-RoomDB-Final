package com.example.myapplication.room.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.myapplication.model.Inventory


import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update

@Dao
interface InventoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Inventory): Long

    @Update
    suspend fun update(item: Inventory): Int

    @Delete
    suspend fun delete(item: Inventory): Int

    @Query("SELECT * FROM inventory ORDER BY name ASC")
    fun getAll(): LiveData<List<Inventory>>

    @Query("SELECT * FROM inventory WHERE id = :id LIMIT 1")
    fun getById(id: String): LiveData<Inventory?>

    @Query("UPDATE inventory SET stock = stock + :delta, lastModified = :ts WHERE id = :id")
    suspend fun changeStock(id: String, delta: Int, ts: Long): Int

}