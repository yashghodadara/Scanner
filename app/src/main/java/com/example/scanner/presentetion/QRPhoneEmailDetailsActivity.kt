package com.example.scanner.presentetion

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
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
import com.example.scanner.databinding.ActivityQrphoneEmailDetailsBinding
import com.example.scanner.util.BaseActivity
import com.example.scanner.util.CustomTypefaceSpan
import androidx.core.net.toUri

class QRPhoneEmailDetailsActivity : BaseActivity(), View.OnClickListener {
    private lateinit var binding : ActivityQrphoneEmailDetailsBinding
    var type : String? = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityQrphoneEmailDetailsBinding.inflate(layoutInflater)
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
            binding.ivCapturePhoto.setImageURI(Uri.parse(it))
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

        when (type) {
            "Email" -> {
                val info = """
                    To: ${intent.getStringExtra("qr_email_to")}
                    Subject: ${intent.getStringExtra("qr_subject")}
                    Body: ${intent.getStringExtra("qr_body")}
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
                binding.tvType.text = getString(R.string.email)
                binding.ivType.setImageResource(R.drawable.ic_icon_email)

                binding.ivEmailPhone.setImageResource(R.drawable.ic_icon_email)
                binding.tvEmailPhone.text = getString(R.string.email)
            }
            "Phone" -> {
                binding.tvQrContent.text = intent.getStringExtra("qr_number")
                binding.tvType.text = getString(R.string.phone)
                binding.ivType.setImageResource(R.drawable.ic_icon_phone)

                binding.ivEmailPhone.setImageResource(R.drawable.ic_icon_phone)
                binding.tvEmailPhone.text = getString(R.string.phone)
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

        binding.llEmailPhone.setOnClickListener(this)
        binding.llContact.setOnClickListener(this)
        binding.llCopy.setOnClickListener(this)
        binding.llShare.setOnClickListener(this)
        binding.ivFAQ.setOnClickListener(this)
        binding.ivBack.setOnClickListener(this)
        binding.tvFeedback.setOnClickListener(this)
        binding.ivFavorites.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.llEmailPhone -> {
                if (type.equals("Email")){
                    val email = if (type == "EMAIL" && binding.tvQrContent.text.startsWith("mailto:")) {
                        binding.tvQrContent.text.toString().substringAfter("mailto:")
                    } else {
                        binding.tvQrContent.text.toString()
                    }
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = "mailto:$email".toUri()
                        putExtra(Intent.EXTRA_SUBJECT, "QR Code Content")
                        putExtra(Intent.EXTRA_TEXT, binding.tvQrContent.text.toString())
                    }
                    startActivity(Intent.createChooser(intent, "Send email"))

                }else{
                    val phoneNumber = if (binding.tvQrContent.text.startsWith("tel:")) {
                        binding.tvQrContent.text.toString().substringAfter("tel:")
                    } else {
                        binding.tvQrContent.text.toString()
                    }
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = "tel:$phoneNumber".toUri()
                    }
                    startActivity(intent)
                }
            }

            R.id.llContact -> {
                addContact(this,intent.getStringExtra("qr_name"),intent.getStringExtra("qr_phone"),intent.getStringExtra("qr_email"))
            }
            R.id.llCopy -> {
                val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("QR Content", binding.tvQrContent.text.toString())
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, getString(R.string.copied_to_clipboard), Toast.LENGTH_SHORT).show()
            }
            R.id.llShare -> {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, binding.tvQrContent.text.toString())
                }
                startActivity(Intent.createChooser(intent, getString(R.string.share_qr_content)))
            }

            R.id.ivFAQ -> {
                val intent = Intent(this, FAQActivity::class.java)
                startActivity(intent)
            }

            R.id.ivBack -> {
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


    fun addContact(context: Context, name: String?, phone: String?, email: String?) {
        try {
            val intent = Intent(Intent.ACTION_INSERT).apply {
                setType(ContactsContract.Contacts.CONTENT_TYPE)
                name?.let { putExtra(ContactsContract.Intents.Insert.NAME, it) }
                phone?.let { putExtra(ContactsContract.Intents.Insert.PHONE, it) }
                email?.let { putExtra(ContactsContract.Intents.Insert.EMAIL, it) }
            }


            val altIntent = Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI).apply {
                name?.let { putExtra(ContactsContract.Intents.Insert.NAME, it) }
                phone?.let { putExtra(ContactsContract.Intents.Insert.PHONE, it) }
                email?.let { putExtra(ContactsContract.Intents.Insert.EMAIL, it) }
            }

            try {
                context.startActivity(intent)
                return
            } catch (_: ActivityNotFoundException) {
                try {
                    context.startActivity(altIntent)
                    return
                } catch (_: ActivityNotFoundException) {

                }
            }

            val contactsIntent = context.packageManager.getLaunchIntentForPackage("com.android.contacts")
                ?: context.packageManager.getLaunchIntentForPackage("com.samsung.android.contacts")
                ?: context.packageManager.getLaunchIntentForPackage("com.google.android.contacts")

            if (contactsIntent != null) {
                context.startActivity(contactsIntent)
            } else {
                val viewIntent = Intent(Intent.ACTION_VIEW, ContactsContract.Contacts.CONTENT_URI)
                if (viewIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(viewIntent)
                } else {
                    Toast.makeText(context, "No Contacts app available", Toast.LENGTH_LONG).show()
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
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
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS // removes light icons → shows white icons
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility =
                window.decorView.systemUiVisibility and
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }
    }
}