package com.example.myapplication.repository

import androidx.lifecycle.LiveData
import com.example.myapplication.model.Inventory
import com.example.myapplication.room.dao.InventoryDao

class InventoryRepository(private val dao: InventoryDao) {
    fun getAllInventory(): LiveData<List<Inventory>> = dao.getAll()
    fun getInventoryById(id: String) = dao.getById(id)

    suspend fun insertInventory(i: Inventory) = dao.insert(i)
    suspend fun updateInventory(i: Inventory) = dao.update(i)
    suspend fun deleteInventory(i: Inventory) = dao.delete(i)
    suspend fun changeStock(id: String, delta: Int) = dao.changeStock(id, delta, System.currentTimeMillis())
}