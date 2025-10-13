package com.example.myapplication.repository

import androidx.lifecycle.LiveData
import com.example.myapplication.model.MedicineSchedule
import com.example.myapplication.room.dao.MedicineScheduleDao

class MedicineScheduleRepository(private val dao: MedicineScheduleDao) {
    fun getSchedulesForDate(date: String): LiveData<List<MedicineSchedule>> = dao.getSchedulesForDate(date)

    suspend fun insert(schedule: MedicineSchedule): Long = dao.insert(schedule)
    suspend fun update(schedule: MedicineSchedule): Int = dao.update(schedule)
    suspend fun delete(schedule: MedicineSchedule): Int = dao.delete(schedule)
    suspend fun deleteSchedulesByMedicineId(medicineId: String) = dao.deleteSchedulesByMedicineId(medicineId)
}