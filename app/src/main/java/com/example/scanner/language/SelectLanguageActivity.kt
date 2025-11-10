package com.example.scanner.language

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scanner.home.HomeActivity
import com.example.scanner.R
import com.example.scanner.databinding.ActivitySelectLanguageBinding
import com.example.scanner.util.BaseActivity
import com.example.scanner.util.Constants
import com.example.scanner.util.LocaleManager
import com.example.scanner.util.PreferenceManager

class SelectLanguageActivity : BaseActivity(), View.OnClickListener {
    private lateinit var binding : ActivitySelectLanguageBinding
    private lateinit var adapter: LanguageAdapter
    private lateinit var pref: PreferenceManager
    private var selectedLanguage = ""
    private var selectedCode = ""
    private var from: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySelectLanguageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        hideSystemUIAAndSetStatusBarColor()
        pref = PreferenceManager(this)
        from = intent.getStringExtra(Constants.FROM)
        binding.rvLanguage.layoutManager = LinearLayoutManager(this)
        selectedLanguage = pref.getString(Constants.LANGUAGE)
        selectedCode = LocaleManager.getPersistedLanguage(this)

        if (from == Constants.SPLASH) {
            binding.ivBack.visibility = View.GONE
        }else{
            binding.ivBack.visibility = View.VISIBLE
        }

        if (selectedLanguage.isEmpty() || selectedCode.isEmpty()) {
            val deviceLangCode = LocaleManager.getDeviceDefaultLanguage()

            val supportedLang = when (deviceLangCode) {
                Constants.Language.CODE_ENGLISH -> Constants.Language.ENGLISH
                Constants.Language.CODE_SPANISH -> Constants.Language.SPANISH
                Constants.Language.CODE_FRENCH -> Constants.Language.FRENCH
                Constants.Language.CODE_HINDI -> Constants.Language.HINDI
                Constants.Language.CODE_CHINESE -> Constants.Language.CHINESE
                Constants.Language.CODE_ARABIC -> Constants.Language.ARABIC
                Constants.Language.CODE_RUSSIAN -> Constants.Language.RUSSIAN
                Constants.Language.CODE_PORTUGUESE -> Constants.Language.PORTUGUESE
                Constants.Language.CODE_ITALIAN -> Constants.Language.ITALIAN
                Constants.Language.CODE_GERMAN -> Constants.Language.GERMAN
                Constants.Language.CODE_JAPANESE -> Constants.Language.JAPANESE
                Constants.Language.CODE_URDU -> Constants.Language.URDU
                Constants.Language.CODE_AFRIKAANS -> Constants.Language.AFRIKAANS
                Constants.Language.CODE_AMHARIC -> Constants.Language.AMHARIC
                else -> Constants.Language.ENGLISH
            }

            selectedLanguage = supportedLang
            selectedCode = deviceLangCode

            LocaleManager.persistLanguage(this, selectedCode)
            pref.setString(Constants.LANGUAGE, selectedLanguage)
        }

        val languages = mutableListOf(
            Language(Constants.Language.CODE_ENGLISH,Constants.Language.ENGLISH, Constants.Language.LAN_ENGLISH,R.drawable.flag_english),
            Language(Constants.Language.CODE_SPANISH,Constants.Language.SPANISH, Constants.Language.LAN_SPANISH,R.drawable.flag_spanish),
            Language(Constants.Language.CODE_FRENCH,Constants.Language.FRENCH, Constants.Language.LAN_FRENCH,R.drawable.flag_french),
            Language(Constants.Language.CODE_HINDI,Constants.Language.HINDI, Constants.Language.LAN_HINDI,R.drawable.flag_hindi),
            Language(Constants.Language.CODE_CHINESE,Constants.Language.CHINESE, Constants.Language.LAN_CHINESE,R.drawable.flag_chinese),
            Language(Constants.Language.CODE_ARABIC,Constants.Language.ARABIC, Constants.Language.LAN_ARABIC,R.drawable.flag_arabic),
            Language(Constants.Language.CODE_RUSSIAN,Constants.Language.RUSSIAN, Constants.Language.LAN_RUSSIAN,R.drawable.flag_russian),
            Language(Constants.Language.CODE_PORTUGUESE,Constants.Language.PORTUGUESE, Constants.Language.LAN_PORTUGUESE,R.drawable.flag_portuguese),
            Language(Constants.Language.CODE_ITALIAN,Constants.Language.ITALIAN, Constants.Language.LAN_ITALIAN,R.drawable.flag_italian),
            Language(Constants.Language.CODE_GERMAN,Constants.Language.GERMAN, Constants.Language.LAN_GERMAN,R.drawable.flag_german),
            Language(Constants.Language.CODE_JAPANESE,Constants.Language.JAPANESE, Constants.Language.LAN_JAPANESE,R.drawable.flag_japanese),
            Language(Constants.Language.CODE_URDU,Constants.Language.URDU, Constants.Language.LAN_URDU,R.drawable.flag_urdu),
            Language(Constants.Language.CODE_AFRIKAANS,Constants.Language.AFRIKAANS, Constants.Language.LAN_AFRIKAANS,R.drawable.flag_afrikaans),
            Language(Constants.Language.CODE_AMHARIC,Constants.Language.AMHARIC, Constants.Language.LAN_AMHARIC,R.drawable.flag_amharic),
        )
        adapter = LanguageAdapter(languages, selectedLanguage,selectedCode) { selected ->
            selectedCode = selected.code
            selectedLanguage = selected.languageName
        }
        binding.rvLanguage.adapter = adapter
        binding.btnNext.setOnClickListener(this)
        binding.ivBack.setOnClickListener(this)
    }

    @Suppress("DEPRECATION")
    private fun hideSystemUIAAndSetStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let { controller ->
                controller.hide(
                    WindowInsets.Type.navigationBars()
                )
                controller.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    )
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.setSystemBarsAppearance(
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.btnNext ->{
                if (selectedCode.isEmpty()) return
                LocaleManager.persistLanguage(this, selectedCode)
                LocaleManager.applyLocale(this, selectedCode)
                pref.setString(Constants.LANGUAGE,selectedLanguage)

                if (from == Constants.SPLASH) {
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                }else{
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                }
            }

            R.id.ivBack -> {
//                val intent = Intent(this, SettingsActivity::class.java)
//                startActivity(intent)
//                finish()
            }
        }
    }
}