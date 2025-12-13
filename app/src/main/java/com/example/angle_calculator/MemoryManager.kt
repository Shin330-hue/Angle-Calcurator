package com.example.angle_calculator

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class SavedTriangle(
    val id: Long,
    val name: String,
    val sideA: Double?,
    val sideB: Double?,
    val sideC: Double?,
    val angleA: Double?,
    val angleB: Double?,
    val angleC: Double?
)

object MemoryManager {
    private const val PREFS_NAME = "triangle_memory"
    private const val KEY_TRIANGLES = "saved_triangles"
    private val gson = Gson()

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveTriangle(context: Context, triangle: SavedTriangle) {
        val triangles = loadAllTriangles(context).toMutableList()
        // Remove existing with same ID if updating
        triangles.removeAll { it.id == triangle.id }
        triangles.add(triangle)
        val json = gson.toJson(triangles)
        getPrefs(context).edit().putString(KEY_TRIANGLES, json).apply()
    }

    fun loadAllTriangles(context: Context): List<SavedTriangle> {
        val json = getPrefs(context).getString(KEY_TRIANGLES, null) ?: return emptyList()
        val type = object : TypeToken<List<SavedTriangle>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    fun deleteTriangle(context: Context, id: Long) {
        val triangles = loadAllTriangles(context).toMutableList()
        triangles.removeAll { it.id == id }
        val json = gson.toJson(triangles)
        getPrefs(context).edit().putString(KEY_TRIANGLES, json).apply()
    }
}
