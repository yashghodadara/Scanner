package com.example.scanner.util

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlin.text.isNotEmpty

open class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        val lang = LocaleManager.getPersistedLanguage(newBase)
        val ctx = if (lang.isNotEmpty()) LocaleManager.applyLocale(newBase, lang) else newBase
        super.attachBaseContext(ctx)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}