package com.example.scanner.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class PreferenceManager(context: Context) {
    private val PREF_NAME = "my_app_preferences"
    private val sharedPref: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun setString(key: String, value: String) {
        sharedPref.edit { putString(key, value) }
    }

    fun getString(key: String, default: String = ""): String {
        return sharedPref.getString(key, default) ?: default
    }
}
