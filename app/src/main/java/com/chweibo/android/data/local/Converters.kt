package com.chweibo.android.data.local

import androidx.room.TypeConverter
import com.chweibo.android.data.model.Geo
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        if (value == null) return emptyList()
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromGeo(geo: Geo?): String? {
        return gson.toJson(geo)
    }

    @TypeConverter
    fun toGeo(value: String?): Geo? {
        if (value == null) return null
        return gson.fromJson(value, Geo::class.java)
    }

    @TypeConverter
    fun fromMap(value: Map<String, Any>?): String? {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toMap(value: String?): Map<String, Any>? {
        if (value == null) return null
        val mapType = object : TypeToken<Map<String, Any>>() {}.type
        return gson.fromJson(value, mapType)
    }
}
