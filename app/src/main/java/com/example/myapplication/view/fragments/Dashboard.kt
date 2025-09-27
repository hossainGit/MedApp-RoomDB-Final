package com.example.myapplication.view.fragments

import android.content.Intent
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
import com.example.myapplication.model.Medicine
import com.example.myapplication.room.AppDatabase
import com.example.myapplication.repository.InventoryRepository
import com.example.myapplication.repository.MedicineRepository
import com.example.myapplication.view.activities.AddMedActivity
import com.example.myapplication.viewmodel.InventoryViewModel
import com.example.myapplication.viewmodel.InventoryViewModelFactory
import com.example.myapplication.viewmodel.MedicineViewModel
import com.example.myapplication.viewmodel.MedicineViewModelFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton

class Dashboard : Fragment() {

    private lateinit var medListContainer: LinearLayout
    private lateinit var pillCabinetContainer: LinearLayout
    private lateinit var fab: FloatingActionButton

    private val medicineViewModel: MedicineViewModel by viewModels {
        val db = AppDatabase.getInstance(requireContext())
        val repo = MedicineRepository(db.medicineDao())
        MedicineViewModelFactory(repo)
    }

    private val inventoryViewModel: InventoryViewModel by viewModels {
        val db = AppDatabase.getInstance(requireContext())
        val repo = InventoryRepository(db.inventoryDao())
        InventoryViewModelFactory(repo)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_dashboard, container, false)
        medListContainer = v.findViewById(R.id.medListContainer)
        pillCabinetContainer = v.findViewById(R.id.pillCabinetContainer)
        fab = v.findViewById(R.id.fBtn)

        fab.setOnClickListener {
            val i = Intent(requireActivity(), AddMedActivity::class.java)
            startActivity(i)
        }

        return v
    }

    override fun onResume() {
        super.onResume()
        loadUpcoming()
        loadCabinetPreview()
    }

    private fun loadUpcoming() {
        medicineViewModel.medicines.observe(viewLifecycleOwner, Observer { medicines ->
            medListContainer.removeAllViews()

            val activeMeds = medicines.filter { it.status == "Pending" }

            if (activeMeds.isEmpty()) {
                val t = TextView(requireContext())
                t.text = getString(R.string.no_upcoming_medicines)
                medListContainer.addView(t)
                return@Observer
            }

            val inflater = LayoutInflater.from(requireContext())
            activeMeds.forEach { medicine ->
                val card = inflater.inflate(R.layout.item_dash_med, medListContainer, false)
                val tvName = card.findViewById<TextView>(R.id.tvDashMedName)
                val tvDetails = card.findViewById<TextView>(R.id.tvDashMedDetails)
                val btnTaken = card.findViewById<Button>(R.id.btnDashTaken)
                val btnMissed = card.findViewById<Button>(R.id.btnDashMissed)

                tvName.text = medicine.name
                tvDetails.text = formatMedicineDetails(medicine)

                btnTaken.setOnClickListener {
                    showConfirmationDialog("Mark as Taken", "Mark ${medicine.name} as taken?") {
                        val updatedMed = medicine.copy(
                            status = "Taken",
                            lastModified = System.currentTimeMillis()
                        )
                        medicineViewModel.updateMedicine(updatedMed)
                    }
                }

                btnMissed.setOnClickListener {
                    showConfirmationDialog("Mark as Missed", "Mark ${medicine.name} as missed?") {
                        val updatedMed = medicine.copy(
                            status = "Missed",
                            lastModified = System.currentTimeMillis()
                        )
                        medicineViewModel.updateMedicine(updatedMed)
                    }
                }

                medListContainer.addView(card)
            }
        })
    }

    private fun loadCabinetPreview() {
        inventoryViewModel.inventory.observe(viewLifecycleOwner, Observer { inventory ->
            pillCabinetContainer.removeAllViews()

            if (inventory.isEmpty()) {
                val t = TextView(requireContext()).apply {
                    text = "No cabinet items"
                    setPadding(20, 20, 20, 20)
                }
                pillCabinetContainer.addView(t)
                return@Observer
            }

            val inflater = LayoutInflater.from(requireContext())
            inventory.forEach { item ->
                val card = inflater.inflate(R.layout.item_dash_cab, pillCabinetContainer, false)
                val tvName = card.findViewById<TextView>(R.id.tvDashCabName)
                val tvStock = card.findViewById<TextView>(R.id.tvDashCabStock)

                tvName.text = item.name
                tvStock.text = "Stock: ${item.stock}"

                pillCabinetContainer.addView(card)
            }
        })
    }

    private fun formatMedicineDetails(medicine: Medicine): String {
        val times = medicine.times.joinToString(", ")
        return "$times - ${medicine.mealTiming} | ${medicine.pillsPerDose} tab | ${medicine.dosage}"
    }

    private fun showConfirmationDialog(title: String, message: String, onConfirm: () -> Unit) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Yes") { _, _ -> onConfirm() }
            .setNegativeButton("No", null)
            .show()
    }
}