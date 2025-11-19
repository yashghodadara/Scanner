package com.example.scanner.presentetion

import android.animation.ObjectAnimator
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.animation.DecelerateInterpolator
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.scanner.R
import com.example.scanner.databinding.ActivitySplashBinding
import com.example.scanner.util.BaseActivity
import com.example.scanner.util.Constants
import com.example.scanner.util.PreferenceManager

class SplashActivity : BaseActivity() {
    private lateinit var binding: ActivitySplashBinding
    private var language = ""
    private lateinit var pref: SharedPreferences
    private lateinit var preferenceManager: PreferenceManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        hideSystemUIAAndSetStatusBarColor()
        animateProgressBar()

        pref = getSharedPreferences(Constants.APP_PREFERENCES, MODE_PRIVATE)
        preferenceManager = PreferenceManager(this)
        language = preferenceManager.getString(Constants.LANGUAGE)
    }

    private fun animateProgressBar() {
        ObjectAnimator.ofInt(binding.progressBar, Constants.PROGRESS, 0, 100).apply {
            duration = 3000
            interpolator = DecelerateInterpolator()
            start()
        }
        Handler(Looper.getMainLooper()).postDelayed({
            checkFirstLaunch()
        }, 3000)
    }

    private fun checkFirstLaunch() {
        if (language.isEmpty()) {
                val intent = Intent(this, SelectLanguageActivity::class.java)
                intent.putExtra(Constants.FROM, Constants.SPLASH)
                startActivity(intent)
                finish()

        }else{
            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra(Constants.FROM, Constants.SPLASH)
            startActivity(intent)
            finish()
        }
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

        val bgColor = ContextCompat.getColor(this, R.color.bg_color)
        window.statusBarColor = bgColor

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.setSystemBarsAppearance(
                0,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility =
                window.decorView.systemUiVisibility and
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }
    }
}