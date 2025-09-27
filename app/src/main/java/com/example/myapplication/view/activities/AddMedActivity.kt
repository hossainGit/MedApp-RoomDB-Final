package com.example.myapplication.view.activities

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.CheckBox
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.myapplication.R
import com.example.myapplication.model.Medicine
import com.example.myapplication.model.Inventory
import com.example.myapplication.room.AppDatabase
import com.example.myapplication.repository.InventoryRepository
import com.example.myapplication.repository.MedicineRepository
import com.example.myapplication.viewmodel.InventoryViewModel
import com.example.myapplication.viewmodel.InventoryViewModelFactory
import com.example.myapplication.viewmodel.MedicineViewModel
import com.example.myapplication.viewmodel.MedicineViewModelFactory
import org.json.JSONArray

class AddMedActivity : AppCompatActivity() {

    private lateinit var medName: EditText
    private lateinit var medDosage: EditText
    private lateinit var medPillsPerDose: EditText
    private lateinit var medStock: EditText
    private lateinit var cbMorning: CheckBox
    private lateinit var cbNoon: CheckBox
    private lateinit var cbNight: CheckBox
    private lateinit var rbBefore: RadioButton
    private lateinit var rbAfter: RadioButton
    private lateinit var btnAdd: Button
    private lateinit var btnDiscard: Button

    private var editingId: String? = null
    private lateinit var prefs: SharedPreferences

    private val medicineViewModel: MedicineViewModel by viewModels {
        val db = AppDatabase.getInstance(this)
        val repo = MedicineRepository(db.medicineDao())
        MedicineViewModelFactory(repo)
    }

    private val inventoryViewModel: InventoryViewModel by viewModels {
        val db = AppDatabase.getInstance(this)
        val repo = InventoryRepository(db.inventoryDao())
        InventoryViewModelFactory(repo)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_med)

        prefs = getSharedPreferences("myData", Context.MODE_PRIVATE)

        // Initialize views
        medName = findViewById(R.id.medName)
        medDosage = findViewById(R.id.medDosage)
        medPillsPerDose = findViewById(R.id.medPillsPerDose)
        medStock = findViewById(R.id.medStock)
        cbMorning = findViewById(R.id.checkBox2)
        cbNoon = findViewById(R.id.checkBox3)
        cbNight = findViewById(R.id.checkBox4)
        rbBefore = findViewById(R.id.radioButton6)
        rbAfter = findViewById(R.id.radioButton7)
        btnAdd = findViewById(R.id.addMedButton)
        btnDiscard = findViewById(R.id.discardBtn)

        // Check if editing existing medicine
        editingId = intent.getStringExtra("medId")
        if (editingId != null) {
            // Load existing medicine
            medicineViewModel.getMedicineById(editingId!!).observe(this, Observer { medicine ->
                medicine?.let { prefillFields(it) }
            })
            btnAdd.text = "Update Medication"
        }

        btnDiscard.setOnClickListener { finish() }
        btnAdd.setOnClickListener { onSave() }
    }

    override fun onResume() {
        super.onResume()
        // Restore draft only if not editing existing medicine
        if (editingId == null) {
            restoreDraft()
        }
    }

    override fun onPause() {
        super.onPause()
        // Save draft only if not editing existing medicine
        if (editingId == null) {
            saveDraft()
        }
    }

    private fun prefillFields(medicine: Medicine) {
        medName.setText(medicine.name)
        medDosage.setText(medicine.dosage)
        medPillsPerDose.setText(medicine.pillsPerDose.toString())

        // Set time checkboxes
        cbMorning.isChecked = medicine.times.contains("Morning")
        cbNoon.isChecked = medicine.times.contains("Noon")
        cbNight.isChecked = medicine.times.contains("Night")

        // Set meal timing radio buttons
        if (medicine.mealTiming.contains("Before", true)) {
            rbBefore.isChecked = true
        } else {
            rbAfter.isChecked = true
        }

        // Set stock - try to get from inventory
        medicine.inventoryId?.let { inventoryId ->
            inventoryViewModel.getInventoryById(inventoryId).observe(this, Observer { inventory ->
                inventory?.let {
                    medStock.setText(it.stock.toString())
                }
            })
        } ?: run {
            medStock.setText("0")
        }
    }

    private fun saveDraft() {
        val editor = prefs.edit()
        editor.putString("draft_med_name", medName.text.toString())
        editor.putString("draft_med_dosage", medDosage.text.toString())
        editor.putString("draft_med_pills", medPillsPerDose.text.toString())
        editor.putString("draft_med_stock", medStock.text.toString())

        // Save times as JSON array
        val times = mutableListOf<String>()
        if (cbMorning.isChecked) times.add("Morning")
        if (cbNoon.isChecked) times.add("Noon")
        if (cbNight.isChecked) times.add("Night")
        val timesJson = JSONArray(times).toString()
        editor.putString("draft_med_times", timesJson)

        editor.apply()
    }

    private fun restoreDraft() {
        medName.setText(prefs.getString("draft_med_name", ""))
        medDosage.setText(prefs.getString("draft_med_dosage", ""))
        medPillsPerDose.setText(prefs.getString("draft_med_pills", ""))
        medStock.setText(prefs.getString("draft_med_stock", ""))

        // Restore times from JSON
        val timesJson = prefs.getString("draft_med_times", "[]")
        try {
            val arr = JSONArray(timesJson)
            for (i in 0 until arr.length()) {
                when (arr.getString(i)) {
                    "Morning" -> cbMorning.isChecked = true
                    "Noon" -> cbNoon.isChecked = true
                    "Night" -> cbNight.isChecked = true
                }
            }
        } catch (e: Exception) {
            // Ignore JSON parsing errors
        }
    }

    private fun onSave() {
        val name = medName.text.toString().trim()
        val dosage = medDosage.text.toString().trim()
        val pillsPerDose = medPillsPerDose.text.toString().toIntOrNull() ?: 1
        val stock = medStock.text.toString().toIntOrNull() ?: 0

        // Validate inputs
        if (name.isEmpty() || dosage.isEmpty()) {
            Toast.makeText(this, "Please fill name and dosage", Toast.LENGTH_SHORT).show()
            return
        }

        val times = mutableListOf<String>()
        if (cbMorning.isChecked) times.add("Morning")
        if (cbNoon.isChecked) times.add("Noon")
        if (cbNight.isChecked) times.add("Night")

        if (times.isEmpty()) {
            Toast.makeText(this, "Select at least one time", Toast.LENGTH_SHORT).show()
            return
        }

        val mealTiming = if (rbBefore.isChecked) "Before Meal" else "After Meal"
        val id = editingId ?: System.currentTimeMillis().toString()

        val medicine = Medicine(
            id = id,
            name = name,
            dosage = dosage,
            pillsPerDose = pillsPerDose,
            times = times,
            mealTiming = mealTiming,
            status = "Pending",
            inventoryId = if (stock > 0) "inv_$id" else null,
            lastModified = System.currentTimeMillis()
        )

        AlertDialog.Builder(this)
            .setTitle("Confirm")
            .setMessage(if (editingId != null) "Update medication?" else "Save medication?")
            .setPositiveButton("Yes") { _, _ ->
                if (editingId != null) {
                    medicineViewModel.updateMedicine(medicine)
                    // Update inventory if stock exists
                    if (stock > 0) {
                        val inventory = Inventory(
                            id = "inv_$id",
                            name = name,
                            unit = "tab",
                            stock = stock,
                            lastModified = System.currentTimeMillis()
                        )
                        inventoryViewModel.updateInventory(inventory)
                    }
                } else {
                    medicineViewModel.insertMedicine(medicine)
                    // Create inventory item if stock is provided
                    if (stock > 0) {
                        val inventory = Inventory(
                            id = "inv_$id",
                            name = name,
                            unit = "tab",
                            stock = stock,
                            lastModified = System.currentTimeMillis()
                        )
                        inventoryViewModel.insertInventory(inventory)
                    }
                    // Clear draft after successful save
                    prefs.edit().remove("draft_med_name")
                        .remove("draft_med_dosage")
                        .remove("draft_med_pills")
                        .remove("draft_med_stock")
                        .remove("draft_med_times")
                        .apply()
                }
                Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
                finish()
            }
            .setNegativeButton("No", null)
            .show()
    }
}