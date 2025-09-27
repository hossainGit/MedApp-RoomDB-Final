package com.example.myapplication.repository

import androidx.lifecycle.LiveData
import com.example.myapplication.model.Medicine
import com.example.myapplication.room.dao.MedicineDao

class MedicineRepository(private val dao: MedicineDao) {
    fun getAllMedicines(): LiveData<List<Medicine>> = dao.getAll()
    fun getMedicineById(id: String) = dao.getById(id)

    suspend fun insertMedicine(m: Medicine) { dao.insert(m) }
    suspend fun updateMedicine(m: Medicine) { dao.update(m) }
    suspend fun deleteMedicine(m: Medicine) { dao.delete(m) }
}