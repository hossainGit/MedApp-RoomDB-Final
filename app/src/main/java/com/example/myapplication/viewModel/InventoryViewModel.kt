package com.example.myapplication.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.model.Inventory
import com.example.myapplication.repository.InventoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class InventoryViewModel(private val repo: InventoryRepository) : ViewModel() {
    val inventory: LiveData<List<Inventory>> = repo.getAllInventory()

    fun insertInventory(i: Inventory) = viewModelScope.launch(Dispatchers.IO) { repo.insertInventory(i) }
    fun updateInventory(i: Inventory) = viewModelScope.launch(Dispatchers.IO) { repo.updateInventory(i) }
    fun deleteInventory(i: Inventory) = viewModelScope.launch(Dispatchers.IO) { repo.deleteInventory(i) }
    fun changeStock(id: String, delta: Int) = viewModelScope.launch(Dispatchers.IO) { repo.changeStock(id, delta) }
    fun getInventoryById(id: String) = repo.getInventoryById(id)
}