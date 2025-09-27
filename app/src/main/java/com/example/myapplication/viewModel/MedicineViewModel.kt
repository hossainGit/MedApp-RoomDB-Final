package com.example.myapplication.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.model.Medicine
import com.example.myapplication.repository.MedicineRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MedicineViewModel(private val repo: MedicineRepository) : ViewModel() {
    val medicines: LiveData<List<Medicine>> = repo.getAllMedicines()

    fun insertMedicine(m: Medicine) = viewModelScope.launch(Dispatchers.IO) { repo.insertMedicine(m) }
    fun updateMedicine(m: Medicine) = viewModelScope.launch(Dispatchers.IO) { repo.updateMedicine(m) }
    fun deleteMedicine(m: Medicine) = viewModelScope.launch(Dispatchers.IO) { repo.deleteMedicine(m) }
    fun getMedicineById(id: String) = repo.getMedicineById(id)
}