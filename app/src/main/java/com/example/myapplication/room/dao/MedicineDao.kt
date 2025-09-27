package com.example.myapplication.room.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.myapplication.model.Medicine

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update
@Dao
interface MedicineDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(medicine: Medicine): Long

    @Update
    suspend fun update(medicine: Medicine): Int

    @Delete
    suspend fun delete(medicine: Medicine): Int

    @Query("SELECT * FROM medicines ORDER BY lastModified DESC")
    fun getAll(): LiveData<List<Medicine>>

    @Query("SELECT * FROM medicines WHERE id = :id LIMIT 1")
    fun getById(id: String): LiveData<Medicine?>
}