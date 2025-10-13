package com.example.myapplication.utils

import java.text.SimpleDateFormat
import java.util.*

object TimeShiftManager {
    const val SHIFT_MORNING = "MORNING"
    const val SHIFT_NOON = "NOON"
    const val SHIFT_NIGHT = "NIGHT"

    fun getCurrentShift(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when {
            hour in 6..11 -> SHIFT_MORNING
            hour in 12..16 -> SHIFT_NOON
            else -> SHIFT_NIGHT
        }
    }

    fun getTodayDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    fun getShiftDisplayName(shift: String): String {
        return when (shift) {
            SHIFT_MORNING -> "Morning"
            SHIFT_NOON -> "Noon"
            SHIFT_NIGHT -> "Night"
            else -> shift
        }
    }
}