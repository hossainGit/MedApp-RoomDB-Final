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
import com.example.myapplication.repository.MedicineRepository
import com.example.myapplication.view.activities.AddMedActivity
import com.example.myapplication.viewmodel.MedicineViewModel
import com.example.myapplication.viewmodel.MedicineViewModelFactory

class MediTime : Fragment() {

    private lateinit var morningContainer: LinearLayout
    private lateinit var noonContainer: LinearLayout
    private lateinit var nightContainer: LinearLayout
    private lateinit var btnAdd: Button

    private val viewModel: MedicineViewModel by viewModels {
        val db = AppDatabase.getInstance(requireContext())
        val repo = MedicineRepository(db.medicineDao())
        MedicineViewModelFactory(repo)
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

    override fun onResume() {
        super.onResume()
        loadSchedule()
    }

    private fun loadSchedule() {
        viewModel.medicines.observe(viewLifecycleOwner, Observer { medicines ->
            morningContainer.removeAllViews()
            noonContainer.removeAllViews()
            nightContainer.removeAllViews()

            if (medicines.isEmpty()) {
                val t = TextView(requireContext())
                t.text = getString(R.string.no_reminders_found)
                morningContainer.addView(t)
                return@Observer
            }

            val inflater = LayoutInflater.from(requireContext())
            medicines.forEach { medicine ->
                medicine.times.forEach { timeLabel ->
                    val card = inflater.inflate(R.layout.item_medicine, morningContainer, false)
                    val tvName = card.findViewById<TextView>(R.id.tvMedName)
                    val tvDetails = card.findViewById<TextView>(R.id.tvMedDetails)
                    val tvStatus = card.findViewById<TextView>(R.id.tvMedStatus)
                    val btnEdit = card.findViewById<Button>(R.id.btnMedEdit)
                    val btnDelete = card.findViewById<Button>(R.id.btnMedDelete)

                    tvName.text = medicine.name
                    tvDetails.text = formatMedicineDetails(medicine, timeLabel)
                    tvStatus.text = medicine.status.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase() else it.toString()
                    }

                    btnEdit.setOnClickListener {
                        val i = Intent(requireActivity(), AddMedActivity::class.java)
                        i.putExtra("medId", medicine.id)
                        startActivity(i)
                    }

                    btnDelete.setOnClickListener {
                        showDeleteConfirmation(medicine)
                    }

                    when (timeLabel.lowercase()) {
                        "morning" -> morningContainer.addView(card)
                        "noon" -> noonContainer.addView(card)
                        "night" -> nightContainer.addView(card)
                        else -> noonContainer.addView(card)
                    }
                }
            }

            // Add placeholder text if containers are empty
            if (morningContainer.childCount == 0) {
                val t = TextView(requireContext())
                t.text = getString(R.string.no_morning_medicines)
                morningContainer.addView(t)
            }
            if (noonContainer.childCount == 0) {
                val t = TextView(requireContext())
                t.text = getString(R.string.noon_none)
                noonContainer.addView(t)
            }
            if (nightContainer.childCount == 0) {
                val t = TextView(requireContext())
                t.text = getString(R.string.no_night_medicines)
                nightContainer.addView(t)
            }
        })
    }

    private fun formatMedicineDetails(medicine: Medicine, timeLabel: String): String {
        return "$timeLabel | ${medicine.mealTiming} | ${medicine.pillsPerDose} tab | ${medicine.dosage}"
    }

    private fun showDeleteConfirmation(medicine: Medicine) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.delete))
            .setMessage(getString(R.string.delete_reminder, medicine.name))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                viewModel.deleteMedicine(medicine)
            }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }
}