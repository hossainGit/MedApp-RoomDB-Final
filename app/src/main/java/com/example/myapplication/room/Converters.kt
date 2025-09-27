package com.example.myapplication.room

import androidx.room.TypeConverter
import org.json.JSONArray

class Converters {
    @TypeConverter
    fun fromTimesList(list: List<String>?): String {
        if (list == null) return "[]"
        val arr = JSONArray()
        for (s in list) arr.put(s)
        return arr.toString()
    }

    @TypeConverter
    fun toTimesList(value: String?): List<String> {
        if (value == null || value.isBlank()) return emptyList()
        val arr = JSONArray(value)
        val list = mutableListOf<String>()
        for (i in 0 until arr.length()) {
            list.add(arr.optString(i))
        }
        return list
    }
}