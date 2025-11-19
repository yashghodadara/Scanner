package com.example.scanner.presentetion

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Spannable
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.scanner.R
import com.example.scanner.data.QrAllHistoryDatabase
import com.example.scanner.databinding.ActivityQrurlDetailsBinding
import com.example.scanner.util.BaseActivity
import com.example.scanner.util.Constants
import com.example.scanner.util.CustomTypefaceSpan
import androidx.core.net.toUri

class QRUrlDetailsActivity : BaseActivity(), View.OnClickListener {
    private lateinit var binding : ActivityQrurlDetailsBinding
    var type : String? = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityQrurlDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        hideSystemUIAAndSetStatusBarColor()

         type = intent.getStringExtra("qr_type")
        val imageUri = intent.getStringExtra("qr_image")

        imageUri?.let {
            binding.ivCapturePhoto.setImageURI(it.toUri())
        }

        when (type) {
            "URL" -> {
                val url = intent.getStringExtra("qr_url")
                binding.tvQrContent.text = url
                binding.tvQrContent.setTextColor(ContextCompat.getColor(this, R.color.txt_color_blue))
                binding.tvQrContent.setOnClickListener {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        "https://www.google.com/search?q=${Uri.encode(binding.tvQrContent.text.toString())}".toUri()
                    )
                    startActivity(intent)
                }
                binding.tvType.text = getString(R.string.url)
                binding.ivType.setImageResource(R.drawable.ic_icon_url)

                binding.ivOpen.setImageResource(R.drawable.ic_icon_open)
                binding.tvOpen.text = getString(R.string.web_search)
            }
            "WiFi" -> {
                val info = """
                    ssid: ${intent.getStringExtra("qr_ssid")}
                    password: ${intent.getStringExtra("qr_password")}
                    encryptionType: ${intent.getStringExtra("qr_encryptionType")}             
                """.trimIndent()

                val spannableString = SpannableString(info)

                val titleColor = ContextCompat.getColor(this, R.color.txt_color_grey) // or any color you want
                val valueColor = ContextCompat.getColor(this, R.color.white) // default text color

                val titleTextSize = resources.getDimensionPixelSize(R.dimen.title_text_size)
                val valueTextSize = resources.getDimensionPixelSize(R.dimen.value_text_size)

                val titleTypeface = ResourcesCompat.getFont(this, R.font.inter_bold)
                val valueTypeface = ResourcesCompat.getFont(this, R.font.inter_medium)

                val lines = info.split("\n")
                var currentPosition = 0

                lines.forEach { line ->
                    val colonIndex = line.indexOf(":")
                    if (colonIndex > 0) {
                        spannableString.setSpan(
                            ForegroundColorSpan(titleColor),
                            currentPosition,
                            currentPosition + colonIndex,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )

                        spannableString.setSpan(
                            AbsoluteSizeSpan(titleTextSize),
                            currentPosition,
                            currentPosition + colonIndex,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )

                        spannableString.setSpan(
                            StyleSpan(Typeface.BOLD),
                            currentPosition,
                            currentPosition + colonIndex,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )

                        if (titleTypeface != null) {
                            spannableString.setSpan(
                                CustomTypefaceSpan(titleTypeface),
                                currentPosition,
                                currentPosition + colonIndex,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                        }

                        spannableString.setSpan(
                            ForegroundColorSpan(valueColor),
                            currentPosition + colonIndex + 1,
                            currentPosition + line.length,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )

                        spannableString.setSpan(
                            AbsoluteSizeSpan(valueTextSize),
                            currentPosition + colonIndex + 1,
                            currentPosition + line.length,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )

                        if (valueTypeface != null) {
                            spannableString.setSpan(
                                CustomTypefaceSpan(valueTypeface),
                                currentPosition + colonIndex + 1,
                                currentPosition + line.length,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                        }
                    }
                    currentPosition += line.length + 1
                }
                binding.tvQrContent.text = spannableString
                binding.tvType.text = getString(R.string.wifi)
                binding.ivType.setImageResource(R.drawable.ic_icon_wifi)

                binding.ivOpen.setImageResource(R.drawable.ic_icon_wifi)
                binding.tvOpen.text = getString(R.string.wifi)

            }

            else -> {
                binding.tvQrContent.text = intent.getStringExtra("qr_raw")
                binding.tvType.text = getString(R.string.text)
                binding.ivType.setImageResource(R.drawable.ic_icon_text)
            }
        }

        val db = QrAllHistoryDatabase(this)
        val id = intent.getLongExtra("qr_history_id", -1L)

        if (id != -1L) {
            val item = db.getItemById(id)

            if (item != null && item.isFavorite) {
                binding.ivFavorites.setImageResource(R.drawable.ic_icon_yellow_star)
            } else {
                binding.ivFavorites.setImageResource(R.drawable.ic_icon_white_star)
            }
        }

        val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val isBeepEnabled = prefs.getBoolean("isCopy", false)

        if (isBeepEnabled) {
            val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("QR Content", binding.tvQrContent.text.toString())
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show()
        }

        binding.llOpen.setOnClickListener(this)
        binding.llCopy.setOnClickListener(this)
        binding.llShare.setOnClickListener(this)
        binding.ivFAQ.setOnClickListener(this)
        binding.ivBack.setOnClickListener(this)
        binding.tvFeedback.setOnClickListener(this)
        binding.ivFavorites.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.llOpen -> {
                if (type.equals("URL")){
                     intent = Intent(this, WebSearchActivity::class.java)
                    intent.putExtra(Constants.SEARCH_TEXT, binding.tvQrContent.text.toString())
                    startActivity(intent)
                }else{
                    connectToWifi()
                }
            }
            R.id.llCopy -> {
                val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("QR Content", binding.tvQrContent.text.toString())
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show()
            }
            R.id.llShare -> {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, binding.tvQrContent.text.toString())
                }
                startActivity(Intent.createChooser(intent, "Share QR Content"))
            }
            R.id.ivFAQ -> {
                val intent = Intent(this, FAQActivity::class.java)
                startActivity(intent)
            }
            R.id.ivBack ->{
                finish()
            }
            R.id.tvFeedback -> {
                val intent = Intent(this, FeedbackActivity::class.java)
                startActivity(intent)
            }
            R.id.ivFavorites -> {
                val historyDb = QrAllHistoryDatabase(this)
                val historyId = intent.getLongExtra("qr_history_id", -1L)
                if (historyId == -1L) {
                    Toast.makeText(this, "Invalid history id", Toast.LENGTH_SHORT).show()
                    return
                }

                val currentItem = historyDb.getItemById(historyId)
                val isFavoriteNow = currentItem?.isFavorite ?: false

                if (isFavoriteNow) {
                    if (historyDb.updateFavoriteStatus(historyId, false)) {
                        binding.ivFavorites.setImageResource(R.drawable.ic_icon_white_star)
                        Toast.makeText(this, "Removed from Favorites", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Failed to update", Toast.LENGTH_SHORT).show()
                    }
                    return
                }

                if (historyDb.updateFavoriteStatus(historyId, true)) {
                    binding.ivFavorites.setImageResource(R.drawable.ic_icon_yellow_star)
                    Toast.makeText(this, "Added to Favorites", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to update", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun connectToWifi() {
        try {
            val ssid = intent.getStringExtra("qr_ssid")!!
            val encryptionType = intent.getStringExtra("qr_encryptionType")!!
            val password = intent.getStringExtra("qr_password") ?: "WPA"

            if (ssid.isEmpty()) {
                Toast.makeText(this, "WiFi SSID not found", Toast.LENGTH_SHORT).show()
                return
            }

            if (connectToWifiUsingWifiManager(ssid, password, encryptionType)) {
                return
            }
            openWifiSettings(ssid)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to connect to WiFi", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("MissingPermission")
    private fun connectToWifiUsingWifiManager(ssid: String, password: String, encryptionType: String): Boolean {
        return try {
            val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager

            if (!wifiManager.isWifiEnabled) {
                wifiManager.isWifiEnabled = true
            }

            val wifiConfig = WifiConfiguration().apply {
                this.SSID = "\"$ssid\""

                when (encryptionType.uppercase()) {
                    "WEP" -> {
                        wepKeys[0] = "\"$password\""
                        wepTxKeyIndex = 0
                        allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
                        allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40)
                    }
                    "NONE", "OPEN" -> {
                        allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
                    }
                    else -> {
                        preSharedKey = "\"$password\""
                    }
                }
            }
            val networkId = wifiManager.addNetwork(wifiConfig)
            if (networkId != -1) {
                wifiManager.disconnect()
                wifiManager.enableNetwork(networkId, true)
                wifiManager.reconnect()
                Toast.makeText(this, "Connecting to $ssid...", Toast.LENGTH_SHORT).show()
                true
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    private fun openWifiSettings(ssid: String) {
        try {
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
            startActivity(intent)
            Toast.makeText(this, "Please connect to WiFi: $ssid", Toast.LENGTH_LONG).show()
        } catch (_: Exception) {
            val intent = Intent(Settings.ACTION_SETTINGS)
            startActivity(intent)
            Toast.makeText(this, "Please go to WiFi settings and connect to: $ssid", Toast.LENGTH_LONG).show()
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