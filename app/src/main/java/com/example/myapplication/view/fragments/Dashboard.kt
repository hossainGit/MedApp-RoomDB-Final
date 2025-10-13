package com.example.myapplication.view.fragments

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

    private val scheduleViewModel: MedicineScheduleViewModel by viewModels {
        val db = AppDatabase.getInstance(requireContext())
        val scheduleRepo = MedicineScheduleRepository(db.medicineScheduleDao())
        val inventoryRepo = InventoryRepository(db.inventoryDao())  // Add this
        MedicineScheduleViewModelFactory(scheduleRepo, inventoryRepo)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_dashboard, container, false)
        medListContainer = v.findViewById(R.id.medListContainer)
        pillCabinetContainer = v.findViewById(R.id.pillCabinetContainer)
        fab = v.findViewById(R.id.fBtn)

        fab.setOnClickListener {
            val i = android.content.Intent(requireActivity(), AddMedActivity::class.java)
            startActivity(i)
        }

        return v
    }

    override fun onResume() {
        super.onResume()
        loadCurrentMedicines()
        loadCabinetPreview()
    }

    private fun loadCurrentMedicines() {
        val today = TimeShiftManager.getTodayDate()
        val currentShift = TimeShiftManager.getCurrentShift()

        scheduleViewModel.getSchedulesForDate(today).observe(viewLifecycleOwner, Observer { schedules ->
            medListContainer.removeAllViews()

            if (schedules.isEmpty()) {
                showEmptyState()
                return@Observer
            }

            // Get all medicines for display
            medicineViewModel.medicines.observe(viewLifecycleOwner, Observer { medicines ->
                renderMedicines(schedules, medicines, currentShift, today)
            })
        })
    }

    private fun renderMedicines(schedules: List<MedicineSchedule>, medicines: List<Medicine>, currentShift: String, today: String) {
        // Dashboard: Only show PENDING and MISSED medicines (hide TAKEN)
        val currentShiftSchedules = schedules.filter {
            it.shift == currentShift && it.status == "PENDING"
        }
        val overdueSchedules = schedules.filter {
            it.status == "MISSED" || (it.shift != currentShift && it.status == "PENDING")
        }

        if (currentShiftSchedules.isEmpty() && overdueSchedules.isEmpty()) {
            showAllCaughtUp()
            return
        }

        // Show current shift medicines (White Cards - item_dash_med.xml)
        if (currentShiftSchedules.isNotEmpty()) {
            addSectionHeader("Current Shift - ${TimeShiftManager.getShiftDisplayName(currentShift)}")
            currentShiftSchedules.forEach { schedule ->
                medicines.find { it.id == schedule.medicineId }?.let { medicine ->
                    createMedicineCard(medicine, schedule, false)
                }
            }
        }

        // Show overdue medicines (Red Cards - item_dash_med_overdue.xml)
        if (overdueSchedules.isNotEmpty()) {
            addSectionHeader("Overdue Medicines")
            overdueSchedules.forEach { schedule ->
                medicines.find { it.id == schedule.medicineId }?.let { medicine ->
                    createMedicineCard(medicine, schedule, true)
                }
            }
        }
    }

    private fun createMedicineCard(medicine: Medicine, schedule: MedicineSchedule, isOverdue: Boolean) {
        val inflater = LayoutInflater.from(requireContext())

        // Use different layout for overdue cards
        val card = if (isOverdue) {
            inflater.inflate(R.layout.item_dash_med_overdue, medListContainer, false)
        } else {
            inflater.inflate(R.layout.item_dash_med, medListContainer, false)
        }

        val tvName = card.findViewById<TextView>(R.id.tvDashMedName)
        val tvDetails = card.findViewById<TextView>(R.id.tvDashMedDetails)
        val btnTaken = card.findViewById<Button>(R.id.btnDashTaken)
        val btnMissed = card.findViewById<Button>(R.id.btnDashMissed)

        tvName.text = "${medicine.name} ${medicine.dosage} mg"
        tvDetails.text = "Take ${medicine.pillsPerDose} pill(s) ${medicine.mealTiming}"

        if (isOverdue) {
            // Overdue card - only show "Take Now" button
            btnTaken.text = "Take Now"
            btnMissed.text = "Skip Dose"
            btnMissed.visibility = View.VISIBLE

            tvDetails.text = "You missed ${medicine.pillsPerDose} pill(s) ${medicine.mealTiming} at ${schedule.shift}"

            btnTaken.setOnClickListener {
                scheduleViewModel.markTakenLate(schedule)
                // Card will disappear on next refresh (because status becomes TAKEN)
            }

            btnMissed.setOnClickListener {
                scheduleViewModel.markSkipped(schedule)
            }

        } else {
            // Current shift card - show both buttons
            btnTaken.text = "Taken"
            btnMissed.visibility = View.VISIBLE


            btnTaken.setOnClickListener {
                scheduleViewModel.markTaken(schedule)
                // Card will disappear on next refresh (because status becomes TAKEN)
            }

            btnMissed.setOnClickListener {
                scheduleViewModel.markMissed(schedule)
                // Card will turn red on next refresh (because status becomes MISSED)
            }
        }

        medListContainer.addView(card)


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

    private fun addSectionHeader(title: String) {
        val header = TextView(requireContext()).apply {
            text = title
            textSize = 18f
            setTextColor(resources.getColor(android.R.color.black))
            setPadding(20, 30, 20, 10)
        }
        medListContainer.addView(header)
    }

    private fun showEmptyState() {
        val emptyText = TextView(requireContext()).apply {
            text = "No medicines scheduled for today\nAdd medicines to get started"
            textSize = 16f
            setPadding(50, 100, 50, 50)
        }
        medListContainer.addView(emptyText)
    }

    private fun showAllCaughtUp() {
        val caughtUpText = TextView(requireContext()).apply {
            text = "🎉 All caught up for today!"
            textSize = 18f
            setPadding(50, 100, 50, 50)
        }
        medListContainer.addView(caughtUpText)
    }
}