package com.example.myapplication.view.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Switch
import androidx.fragment.app.Fragment
import com.example.myapplication.R

class Settings : Fragment() {

    private lateinit var etName: EditText
    private lateinit var etAge: EditText
    private lateinit var etEmail: EditText
    private lateinit var swRem: Switch
    private lateinit var prefs: SharedPreferences

    companion object {
        private const val PREFS_NAME = "myData"
        private const val KEY_NAME = "profile_name"
        private const val KEY_AGE = "profile_age"
        private const val KEY_EMAIL = "profile_email"
        private const val KEY_REM = "profile_reminder_enabled"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_settings, container, false)

        prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        etName = v.findViewById(R.id.editTextText)
        etAge = v.findViewById(R.id.editTextText2)
        etEmail = v.findViewById(R.id.editTextTextEmailAddress)
        swRem = v.findViewById(R.id.switch1)

        return v
    }

    override fun onResume() {
        super.onResume()
        readSettings()
    }

    override fun onPause() {
        super.onPause()
        saveSettings()
    }

    private fun readSettings() {
        etName.setText(prefs.getString(KEY_NAME, ""))
        etAge.setText(prefs.getString(KEY_AGE, ""))
        etEmail.setText(prefs.getString(KEY_EMAIL, ""))
        swRem.isChecked = prefs.getBoolean(KEY_REM, true)
    }

    private fun saveSettings() {
        val editor = prefs.edit()
        editor.putString(KEY_NAME, etName.text.toString().trim())
        editor.putString(KEY_AGE, etAge.text.toString().trim())
        editor.putString(KEY_EMAIL, etEmail.text.toString().trim())
        editor.putBoolean(KEY_REM, swRem.isChecked)
        editor.apply()
    }
}