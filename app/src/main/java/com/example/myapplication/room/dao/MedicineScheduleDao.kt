package com.example.myapplication.room.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.myapplication.model.MedicineSchedule

@Dao
interface MedicineScheduleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(schedule: MedicineSchedule): Long

    @Update
    suspend fun update(schedule: MedicineSchedule): Int

    @Delete
    suspend fun delete(schedule: MedicineSchedule): Int

    @Query("SELECT * FROM medicine_schedule WHERE date = :date ORDER BY shift ASC")
    fun getSchedulesForDate(date: String): LiveData<List<MedicineSchedule>>

    @Query("SELECT * FROM medicine_schedule WHERE medicineId = :medicineId AND date = :date AND shift = :shift")
    suspend fun getSchedule(medicineId: String, date: String, shift: String): MedicineSchedule?

    @Query("DELETE FROM medicine_schedule WHERE medicineId = :medicineId")
    suspend fun deleteByMedicineId(medicineId: String)

    // Add this method to match your current code
    @Query("DELETE FROM medicine_schedule WHERE medicineId = :medicineId")
    suspend fun deleteSchedulesByMedicineId(medicineId: String)
}