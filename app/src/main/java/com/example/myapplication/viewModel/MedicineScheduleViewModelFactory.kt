package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.repository.MedicineScheduleRepository
import com.example.myapplication.repository.InventoryRepository

class MedicineScheduleViewModelFactory(
    private val scheduleRepo: MedicineScheduleRepository,
    private val inventoryRepo: InventoryRepository  // Add this parameter
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MedicineScheduleViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MedicineScheduleViewModel(scheduleRepo, inventoryRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}