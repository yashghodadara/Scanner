package com.example.scanner.util

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.*
import kotlin.text.isEmpty

object LocaleManager {
    private const val PREFS_NAME = Constants.APP_PREFERENCES
    private const val KEY_LANG = Constants.LANGUAGE

    fun getPersistedLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANG, "") ?: ""
    }


    fun persistLanguage(context: Context, language: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANG, language).apply()
    }
    fun getDeviceDefaultLanguage(): String {
        return Locale.getDefault().language
    }

    fun applyLocale(context: Context, language: String): Context {
        if (language.isEmpty()) return context

        val locale = Locale(language)
        Locale.setDefault(locale)

        val res = context.resources
        val config = Configuration(res.configuration)

        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale)
            config.setLayoutDirection(locale)
            return context.createConfigurationContext(config)
        } else {
            config.locale = locale
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                config.setLayoutDirection(locale)
            }
            res.updateConfiguration(config, res.displayMetrics)
            return context
        }
    }
}