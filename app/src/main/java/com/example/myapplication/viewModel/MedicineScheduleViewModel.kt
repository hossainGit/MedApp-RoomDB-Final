package com.example.myapplication.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.model.MedicineSchedule
import com.example.myapplication.repository.MedicineScheduleRepository
import com.example.myapplication.repository.InventoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MedicineScheduleViewModel(
    private val scheduleRepo: MedicineScheduleRepository,
    private val inventoryRepo: InventoryRepository  // Add inventory repository
) : ViewModel() {

    fun getSchedulesForDate(date: String): LiveData<List<MedicineSchedule>> {
        return scheduleRepo.getSchedulesForDate(date)
    }

    fun markTaken(schedule: MedicineSchedule) = viewModelScope.launch(Dispatchers.IO) {
        val updated = schedule.copy(status = "TAKEN")
        scheduleRepo.update(updated)

        // Update cabinet stock when medicine is taken
        updateCabinetStock(schedule.medicineId)
    }

    fun markTakenLate(schedule: MedicineSchedule) = viewModelScope.launch(Dispatchers.IO) {
        val updated = schedule.copy(status = "TAKEN LATE")
        scheduleRepo.update(updated)

        // Update cabinet stock when medicine is taken
        updateCabinetStock(schedule.medicineId)
    }

    fun markMissed(schedule: MedicineSchedule) = viewModelScope.launch(Dispatchers.IO) {
        val updated = schedule.copy(status = "MISSED")
        scheduleRepo.update(updated)
    }

    fun markSkipped(schedule: MedicineSchedule) = viewModelScope.launch(Dispatchers.IO) {
        val updated = schedule.copy(status = "MISSED AND SKIPPED")
        scheduleRepo.update(updated)

        // Update cabinet stock when medicine is taken
        updateCabinetStock(schedule.medicineId)
    }




    fun deleteSchedulesByMedicineId(medicineId: String) = viewModelScope.launch(Dispatchers.IO) {
        scheduleRepo.deleteSchedulesByMedicineId(medicineId)
    }

    fun createSchedule(medicineId: String, date: String, shift: String) = viewModelScope.launch(Dispatchers.IO) {
        val schedule = MedicineSchedule(
            id = "${medicineId}_${date}_$shift",
            medicineId = medicineId,
            date = date,
            shift = shift,
            status = "PENDING",
            createdAt = System.currentTimeMillis()
        )
        scheduleRepo.insert(schedule)
    }

    // Add method to update cabinet stock
    private suspend fun updateCabinetStock(medicineId: String) {
        val inventoryId = "inv_$medicineId" // Ensure this mapping is correct for your app
        // Atomically decrement stock by 1 and update lastModified
        inventoryRepo.changeStock(inventoryId, -1)
    }


}