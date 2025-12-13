package com.example.angle_calculator

import android.content.Context
import android.content.SharedPreferences

object SettingsManager {
    private const val PREFS_NAME = "angle_calculator_settings"
    private const val KEY_ANGLE_DECIMALS = "angle_decimals"
    private const val KEY_SIDE_DECIMALS = "side_decimals"
    
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    fun getAngleDecimals(context: Context): Int {
        return getPrefs(context).getInt(KEY_ANGLE_DECIMALS, 2)
    }
    
    fun setAngleDecimals(context: Context, decimals: Int) {
        getPrefs(context).edit().putInt(KEY_ANGLE_DECIMALS, decimals).apply()
    }
    
    fun getSideDecimals(context: Context): Int {
        return getPrefs(context).getInt(KEY_SIDE_DECIMALS, 2)
    }
    
    fun setSideDecimals(context: Context, decimals: Int) {
        getPrefs(context).edit().putInt(KEY_SIDE_DECIMALS, decimals).apply()
    }
}
