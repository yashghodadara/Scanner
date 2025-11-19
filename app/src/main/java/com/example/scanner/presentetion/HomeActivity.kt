package com.example.scanner.presentetion

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.scanner.R
import com.example.scanner.databinding.ActivityHomeBinding
import com.example.scanner.databinding.BottomsheetExitBinding
import com.example.scanner.databinding.BottomsheetScanTipsBinding
import com.example.scanner.util.BaseActivity
import com.example.scanner.util.Constants
import com.example.scanner.util.PreferenceManager
import com.google.android.material.bottomsheet.BottomSheetDialog

class HomeActivity : BaseActivity(), View.OnClickListener {
    private lateinit var binding : ActivityHomeBinding
    private var selectedTextColor = 0
    private var unselectedTextColor = 0
    private var isOpenTipsDialog = ""
    private var currentFragmentTag = ""
    private lateinit var pref: PreferenceManager

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
        pref = PreferenceManager(this)
        isOpenTipsDialog = pref.getString(Constants.TIPS_DIALOG)

        if(isOpenTipsDialog.isEmpty()) {
            tipsBottomSheet()
        }

        selectedTextColor =  ContextCompat.getColor(this, R.color.txt_color_blue)
        unselectedTextColor =  ContextCompat.getColor(this, R.color.txt_color_grey)
        val openFragment = intent.getStringExtra(Constants.OPEN_FRAGMENT)

        when(openFragment){
            Constants.SETTINGS_FRAGMENT -> {
                loadFragment(SettingsFragment(), "SETTINGS")
                binding.ivSettings.setImageResource(R.drawable.ic_icon_settings_selected)
                binding.tvSettings.setTextColor(selectedTextColor)

                binding.icScan.setImageResource(R.drawable.ic_icon_scan_unselected)
                binding.tvScan.setTextColor(unselectedTextColor)

                binding.icHistory.setImageResource(R.drawable.ic_icon_history_unselected)
                binding.tvHistory.setTextColor(unselectedTextColor)
            }

            else -> {
                loadFragment(ScanFragment(), "SCAN")
                binding.icScan.setImageResource(R.drawable.ic_icon_scan_selected)
                binding.tvScan.setTextColor(selectedTextColor)

                binding.ivSettings.setImageResource(R.drawable.ic_icon_settings_unselected)
                binding.tvSettings.setTextColor(unselectedTextColor)

                binding.icHistory.setImageResource(R.drawable.ic_icon_history_unselected)
                binding.tvHistory.setTextColor(unselectedTextColor)
            }
        }

        binding.llScan.setOnClickListener(this)
        binding.llSettings.setOnClickListener(this)
        binding.llHistory.setOnClickListener(this)

        onBackPressedDispatcher.addCallback(this) {
                val fragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
                if (fragment is ScanFragment) fragment.pauseScanning()
                showExitDialog()
        }
    }

    private fun showExitDialog() {
        val dialog = BottomSheetDialog(this@HomeActivity)
        val binding = BottomsheetExitBinding.inflate(LayoutInflater.from(this@HomeActivity))
        dialog.setContentView(binding.root)
        dialog.setOnShowListener { dialogInterface ->
            val bottomSheet = (dialogInterface as BottomSheetDialog)
                .findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.setBackgroundResource(android.R.color.transparent)
        }

        binding.btnYes.setOnClickListener{
            dialog.dismiss()
            finishAffinity()
        }
        binding.tvNo.setOnClickListener{dialog.dismiss()}

        dialog.setOnDismissListener {
            if (currentFragmentTag == "SCAN") {
                val fragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
                if (fragment is ScanFragment) fragment.resumeScanning()
            }
        }
        dialog.show()
    }

    private fun tipsBottomSheet() {
        val dialog = BottomSheetDialog(this)
        val binding = BottomsheetScanTipsBinding.inflate(LayoutInflater.from(this))
        dialog.setContentView(binding.root)
        dialog.setOnShowListener { dialogInterface ->
            val bottomSheet = (dialogInterface as BottomSheetDialog)
                .findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.setBackgroundResource(android.R.color.transparent)
        }
        binding.txtOne.text = createStyledAnswer(
            binding.txtOne.context,
            getString(R.string._1_avoid_light_reflection_or_shadow_on_the_code),
            listOf(getString(R.string.reflection), getString(R.string.shadow))
        )

        binding.txtTwo.text = createStyledAnswer(
            binding.txtTwo.context,
            getString(R.string._2_make_your_phone_face_the_code_without_tilting),
            listOf(getString(R.string.without_tilting))
        )

        binding.btnOk.setOnClickListener {
            pref.setString(Constants.TIPS_DIALOG, "Yes")
            dialog.dismiss()
        }
        dialog.show()
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.llScan -> {
                loadFragment(ScanFragment(), "SCAN")
                binding.icScan.setImageResource(R.drawable.ic_icon_scan_selected)
                binding.tvScan.setTextColor(selectedTextColor)

                binding.ivSettings.setImageResource(R.drawable.ic_icon_settings_unselected)
                binding.tvSettings.setTextColor(unselectedTextColor)

                binding.icHistory.setImageResource(R.drawable.ic_icon_history_unselected)
                binding.tvHistory.setTextColor(unselectedTextColor)
            }

            R.id.llSettings -> {
                loadFragment(SettingsFragment(), "SETTINGS")
                binding.ivSettings.setImageResource(R.drawable.ic_icon_settings_selected)
                binding.tvSettings.setTextColor(selectedTextColor)

                binding.icScan.setImageResource(R.drawable.ic_icon_scan_unselected)
                binding.tvScan.setTextColor(unselectedTextColor)

                binding.icHistory.setImageResource(R.drawable.ic_icon_history_unselected)
                binding.tvHistory.setTextColor(unselectedTextColor)
            }
            R.id.llHistory -> {
                loadFragment(HistoryFragment(), "HISTORY")
                binding.icHistory.setImageResource(R.drawable.ic_icon_history_selected)
                binding.tvHistory.setTextColor(selectedTextColor)

                binding.ivSettings.setImageResource(R.drawable.ic_icon_settings_unselected)
                binding.tvSettings.setTextColor(unselectedTextColor)

                binding.icScan.setImageResource(R.drawable.ic_icon_scan_unselected)
                binding.tvScan.setTextColor(unselectedTextColor)
            }
        }
    }

    private fun createStyledAnswer(
        context: Context,
        fullText: String,
        highlights: List<String>
    ): SpannableStringBuilder {

        val spannable = SpannableStringBuilder(fullText)

        val gray = ContextCompat.getColor(context, R.color.txt_color_grey)
        val white = ContextCompat.getColor(context, R.color.white)
        val blue = ContextCompat.getColor(context, R.color.txt_color_blue)

        spannable.setSpan(
            ForegroundColorSpan(gray),
            0,
            fullText.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        for (highlight in highlights) {
            var start = fullText.indexOf(highlight)
            while (start != -1) {
                val isFeedback  = highlight.equals("feedback", ignoreCase = true)
                val colorToUse = if (isFeedback) blue else white
                spannable.setSpan(
                    ForegroundColorSpan(colorToUse),
                    start,
                    start + highlight.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                if (isFeedback) {
                    val clickableSpan = object : ClickableSpan() {
                        override fun onClick(widget: View) {
                            val intent = Intent(context, FeedbackActivity::class.java)
                            startActivity(intent)
                        }

                        override fun updateDrawState(ds: TextPaint) {
                            super.updateDrawState(ds)
                            ds.color = blue
                            ds.isUnderlineText = true
                        }
                    }
                    spannable.setSpan(
                        clickableSpan,
                        start,
                        start + highlight.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
                start = fullText.indexOf(highlight, start + highlight.length)
            }
        }

        return spannable
    }

    private fun loadFragment(fragment: Fragment, tag: String) {
        if (currentFragmentTag == tag) {
            return
        }
        currentFragmentTag = tag
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