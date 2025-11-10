package com.example.scanner.home

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.core.text.HtmlCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.scanner.R
import com.example.scanner.databinding.ActivityHomeBinding
import com.example.scanner.home.fragment.CreateFragment
import com.example.scanner.home.fragment.HistoryFragment
import com.example.scanner.home.fragment.ScanFragment
import com.example.scanner.home.fragment.SettingsFragment
import com.example.scanner.util.BaseActivity

class HomeActivity : BaseActivity(), View.OnClickListener {
    private lateinit var binding : ActivityHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        hideSystemUIAAndSetStatusBarColor()
        loadFragment(ScanFragment())

        binding.bottomNavigationView.apply {
            setPadding(0, 0, 0, 0)

            setOnApplyWindowInsetsListener { view, insets ->
                view.onApplyWindowInsets(insets)
                insets
            }

            layoutParams.height = resources.getDimensionPixelSize(R.dimen.bottom_nav_height)
            itemPaddingTop = 0
            itemPaddingBottom = 0
        }

        binding.bottomNavigationView.itemIconTintList = null
        binding.bottomNavigationView.isItemActiveIndicatorEnabled = false
        binding.bottomNavigationView.itemRippleColor = null

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_scan -> {
                    loadFragment(ScanFragment())
                }

                R.id.nav_history -> {
                    loadFragment(HistoryFragment())
                }

                R.id.nav_create -> {
                    loadFragment(CreateFragment())
                }

                R.id.nav_settings -> {
                    loadFragment(SettingsFragment())
                }
            }
            true
        }

        onBackPressedDispatcher.addCallback(this) {
//            showExitDialog()
        }

    }

    override fun onClick(v: View?) {
        when(v?.id){

        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
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
}