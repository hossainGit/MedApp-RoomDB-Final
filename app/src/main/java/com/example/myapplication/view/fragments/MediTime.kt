package com.example.myapplication.view.fragments

import android.content.Intent
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
import com.example.myapplication.model.Medicine
import com.example.myapplication.model.MedicineSchedule
import com.example.myapplication.room.AppDatabase
import com.example.myapplication.repository.InventoryRepository
import com.example.myapplication.repository.MedicineRepository
import com.example.myapplication.repository.MedicineScheduleRepository
import com.example.myapplication.utils.TimeShiftManager
import com.example.myapplication.view.activities.AddMedActivity
import com.example.myapplication.viewmodel.InventoryViewModel
import com.example.myapplication.viewmodel.InventoryViewModelFactory
import com.example.myapplication.viewmodel.MedicineScheduleViewModel
import com.example.myapplication.viewmodel.MedicineScheduleViewModelFactory
import com.example.myapplication.viewmodel.MedicineViewModel
import com.example.myapplication.viewmodel.MedicineViewModelFactory

class MediTime : Fragment() {

    private lateinit var morningContainer: LinearLayout
    private lateinit var noonContainer: LinearLayout
    private lateinit var nightContainer: LinearLayout
    private lateinit var btnAdd: Button

    private val medicineViewModel: MedicineViewModel by viewModels {
        val db = AppDatabase.getInstance(requireContext())
        val repo = MedicineRepository(db.medicineDao())
        MedicineViewModelFactory(repo)
    }

    private val scheduleViewModel: MedicineScheduleViewModel by viewModels {
        val db = AppDatabase.getInstance(requireContext())
        val scheduleRepo = MedicineScheduleRepository(db.medicineScheduleDao())
        val inventoryRepo = InventoryRepository(db.inventoryDao())
        MedicineScheduleViewModelFactory(scheduleRepo, inventoryRepo)
    }

    private val inventoryViewModel: InventoryViewModel by viewModels {
        val db = AppDatabase.getInstance(requireContext())
        val repo = InventoryRepository(db.inventoryDao())
        InventoryViewModelFactory(repo)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_medi_time, container, false)
        morningContainer = v.findViewById(R.id.morningContainer)
        noonContainer = v.findViewById(R.id.noonContainer)
        nightContainer = v.findViewById(R.id.nightContainer)
        btnAdd = v.findViewById(R.id.button6)

        btnAdd.setOnClickListener {
            val i = Intent(requireActivity(), AddMedActivity::class.java)
            startActivity(i)
        }
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadSchedule()
    }

    override fun onResume() {
        super.onResume()
        loadSchedule()
    }

    private fun loadSchedule() {
        val today = TimeShiftManager.getTodayDate()

        // Clear containers first
        clearContainers()

        // Observe schedules and medicines together
        scheduleViewModel.getSchedulesForDate(today).observe(viewLifecycleOwner, Observer { schedules ->
            medicineViewModel.medicines.observe(viewLifecycleOwner, Observer { medicines ->
                renderSchedules(schedules ?: emptyList(), medicines ?: emptyList())
            })
        })
    }

    private fun clearContainers() {
        morningContainer.removeAllViews()
        noonContainer.removeAllViews()
        nightContainer.removeAllViews()
    }

    private fun renderSchedules(schedules: List<MedicineSchedule>, medicines: List<Medicine>) {
        clearContainers()

        if (schedules.isEmpty()) {
            showEmptyState()
            return
        }

        // Show ALL schedules regardless of status (including TAKEN)
        val morningSchedules = schedules.filter { it.shift == TimeShiftManager.SHIFT_MORNING }
        val noonSchedules = schedules.filter { it.shift == TimeShiftManager.SHIFT_NOON }
        val nightSchedules = schedules.filter { it.shift == TimeShiftManager.SHIFT_NIGHT }

        // Render each shift section
        renderShiftSection(morningContainer, morningSchedules, medicines, "Morning")
        renderShiftSection(noonContainer, noonSchedules, medicines, "Noon")
        renderShiftSection(nightContainer, nightSchedules, medicines, "Night")

        // Show message if all sections are empty
        if (morningSchedules.isEmpty() && noonSchedules.isEmpty() && nightSchedules.isEmpty()) {
            showAllCaughtUp()
        }
    }

    private fun renderShiftSection(container: LinearLayout, schedules: List<MedicineSchedule>, medicines: List<Medicine>, shiftName: String) {
        if (schedules.isEmpty()) {
            val emptyText = TextView(requireContext()).apply {
                text = when (shiftName) {
                    "Morning" -> getString(R.string.no_morning_medicines)
                    "Noon" -> getString(R.string.no_noon_medicines)
                    "Night" -> getString(R.string.no_night_medicines)
                    else -> ""
                }
                setPadding(20, 10, 20, 10)
            }
            container.addView(emptyText)
            return
        }

        schedules.forEach { schedule ->
            medicines.find { it.id == schedule.medicineId }?.let { medicine ->
                createScheduleCard(container, medicine, schedule, shiftName)
            }
        }
    }

    private fun createScheduleCard(container: LinearLayout, medicine: Medicine, schedule: MedicineSchedule, shiftName: String) {
        val inflater = LayoutInflater.from(requireContext())
        val card = inflater.inflate(R.layout.item_medicine, container, false)

        val tvName = card.findViewById<TextView>(R.id.tvMedName)
        val tvDetails = card.findViewById<TextView>(R.id.tvMedDetails)
        val tvDetails2 = card.findViewById<TextView>(R.id.tvMedDetails2)
        val tvStatus = card.findViewById<TextView>(R.id.tvMedStatus)
        val btnEdit = card.findViewById<Button>(R.id.btnMedEdit)
        val btnDelete = card.findViewById<Button>(R.id.btnMedDelete)

        tvName.text = "${medicine.name} ${medicine.dosage} mg"
        tvDetails.text = "${medicine.pillsPerDose} pill(s)"

        when (medicine.mealTiming) {
            "BEFORE_MEAL" -> { tvDetails2.text = "Before Meal"}
            "AFTER_MEAL" -> {tvDetails2.text = "After Meal"}
            else -> {tvDetails2.text = "After Meal"}
        }

        // Set status text and color - show all statuses including TAKEN
        when (schedule.status) {
            "PENDING" -> {
                tvStatus.text = getString(R.string.pending)
                tvStatus.setTextColor(Color.BLUE)
            }
            "MISSED" -> {
                tvStatus.text = getString(R.string.missed)
                tvStatus.setTextColor(Color.RED)
                card.backgroundTintList = requireContext().getColorStateList(R.color.soft_pink)
            }

            "MISSED AND SKIPPED" -> {
                tvStatus.text = getString(R.string.missedSkipped)
                tvStatus.setTextColor(Color.RED)
                card.backgroundTintList = requireContext().getColorStateList(R.color.soft_pink)
            }

            "TAKEN" -> {
                tvStatus.text = getString(R.string.taken)
                tvStatus.setTextColor(Color.BLACK)
                card.elevation = 0f
                card.backgroundTintList = requireContext().getColorStateList(R.color.light_green)
            }

            "TAKEN LATE" -> {
                tvStatus.text = getString(R.string.takenLate)
                tvStatus.setTextColor(Color.BLACK)
                card.elevation = 0f
                card.backgroundTintList = requireContext().getColorStateList(R.color.warm_sand)
            }
        }

        btnEdit.setOnClickListener {
            val i = Intent(requireActivity(), AddMedActivity::class.java)
            i.putExtra("medId", medicine.id)
            startActivity(i)
        }

        btnDelete.setOnClickListener {
            showDeleteConfirmation(medicine)
        }

        container.addView(card)
    }

    private fun showEmptyState() {
        val emptyText = TextView(requireContext()).apply {
            text = getString(R.string.no_medicines_today)
            textSize = 16f
            setPadding(50, 50, 50, 50)
        }
        morningContainer.addView(emptyText)
    }

    private fun showAllCaughtUp() {
        val caughtUpText = TextView(requireContext()).apply {
            text = getString(R.string.all_caught_up)
            textSize = 18f
            setPadding(50, 50, 50, 50)
        }
        morningContainer.addView(caughtUpText)
    }

    private fun showDeleteConfirmation(medicine: Medicine) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.delete_medicine_title))
            .setMessage(getString(R.string.delete_medicine_message, medicine.name))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                // Delete medicine (schedules will cascade delete)
                medicineViewModel.deleteMedicine(medicine)

                // Delete corresponding inventory item
                deleteInventoryForMedicine(medicine.id)

                // Force immediate UI update by reloading
                loadSchedule()
            }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }

    private fun deleteInventoryForMedicine(medicineId: String) {
        val inventoryId = "inv_$medicineId"
        // Use the existing getInventoryById method from your InventoryViewModel
        inventoryViewModel.getInventoryById(inventoryId).observe(viewLifecycleOwner, Observer { inventoryItem ->
            inventoryItem?.let {
                inventoryViewModel.deleteInventory(it)
            }
        })
    }
}