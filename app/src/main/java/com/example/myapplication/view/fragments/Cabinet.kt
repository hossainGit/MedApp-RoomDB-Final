package com.example.myapplication.view.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.myapplication.R
import com.example.myapplication.model.Inventory
import com.example.myapplication.room.AppDatabase
import com.example.myapplication.repository.InventoryRepository
import com.example.myapplication.viewmodel.InventoryViewModel
import com.example.myapplication.viewmodel.InventoryViewModelFactory

class Cabinet : Fragment() {

    private lateinit var container: LinearLayout
    private lateinit var btnUpdate: Button

    private val viewModel: InventoryViewModel by viewModels {
        val db = AppDatabase.getInstance(requireContext())
        val repo = InventoryRepository(db.inventoryDao())
        InventoryViewModelFactory(repo)
    }

    override fun onCreateView(inflater: LayoutInflater, containerParent: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_cabinet, containerParent, false)
        container = v.findViewById(R.id.cabinetContainer)
        btnUpdate = v.findViewById(R.id.button7)

        btnUpdate.setOnClickListener {
            loadCabinet()
        }

        return v
    }

    override fun onResume() {
        super.onResume()
        loadCabinet()
    }

    private fun loadCabinet() {
        viewModel.inventory.observe(viewLifecycleOwner, Observer { inventory ->
            container.removeAllViews()

            if (inventory.isEmpty()) {
                val t = TextView(requireContext()).apply {
                    text = "Cabinet empty\nAdd medications with stock to see them here"
                    textSize = 16f
                    setPadding(50, 50, 50, 50)
                }
                container.addView(t)
                return@Observer
            }

            val inflater = LayoutInflater.from(requireContext())
            inventory.forEach { item ->
                val card = inflater.inflate(R.layout.item_cabinet, container, false)
                val tvName = card.findViewById<TextView>(R.id.tvCabName)
                val tvDetails = card.findViewById<TextView>(R.id.tvCabDetails)
                val tvAvail = card.findViewById<TextView>(R.id.tvIsAvailable)
                val tvStock = card.findViewById<TextView>(R.id.tvStock)
                val btnInc = card.findViewById<Button>(R.id.btnInc)
                val btnDec = card.findViewById<Button>(R.id.btnDec)
                val btnDlt = card.findViewById<Button>(R.id.btnDeleteCabinet)

                tvName.text = item.name
                tvDetails.text = "Inventory - ${item.unit}"
                tvStock.text = item.stock.toString()
                tvAvail.text = when {
                    item.stock <= 0 -> "Empty"
                    item.stock <= 5 -> "Running Low"
                    else -> "Available"
                }
                if (item.stock <= 0) {
                    tvAvail.setTextColor(Color.RED)
                } else if (item.stock <= 5) {
                    tvAvail.setTextColor(Color.parseColor("#FFA500")) // Orange color
                } else {
                    tvAvail.setTextColor(Color.GREEN)
                }

                btnInc.setOnClickListener {
                    viewModel.changeStock(item.id, +1)
                }

                btnDec.setOnClickListener {
                    // Prevent negative stock
                    if (item.stock > 0) {
                        viewModel.changeStock(item.id, -1)
                    }
                }

                btnDlt.setOnClickListener {
                    showDeleteConfirmation(item)
                }

                container.addView(card)
            }
        })
    }

    private fun showDeleteConfirmation(item: Inventory) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Remove")
            .setMessage("Remove ${item.name} from cabinet?")
            .setPositiveButton("Yes") { _, _ ->
                viewModel.deleteInventory(item)
            }
            .setNegativeButton("No", null)
            .show()
    }
}