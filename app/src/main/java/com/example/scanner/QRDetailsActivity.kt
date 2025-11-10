package com.example.scanner

import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.scanner.databinding.ActivityQrdetailsBinding

class QRDetailsActivity : AppCompatActivity() {
    private lateinit var binding :  ActivityQrdetailsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityQrdetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val type = intent.getStringExtra("qr_type")
        val imageUri = intent.getStringExtra("qr_image")

        imageUri?.let {
            binding.ivQrImage.setImageURI(Uri.parse(it))
        }

        when (type) {
            "URL" -> {
                val url = intent.getStringExtra("qr_url")
                binding.tvTitle.text = "Website"
                binding.tvValue.text = url
            }
            "Contact" -> {
                val info = """
                    Name: ${intent.getStringExtra("qr_name")}
                    Organization: ${intent.getStringExtra("qr_organization")}
                    Title: ${intent.getStringExtra("qr_title")}
                    Phone: ${intent.getStringExtra("qr_phone")}
                    Email: ${intent.getStringExtra("qr_email")}
                    Address: ${intent.getStringExtra("qr_address")}
                """.trimIndent()
                binding.tvTitle.text = "Contact Info"
                binding.tvValue.text = info
            }
            "Email" -> {
                val info = """
                    To: ${intent.getStringExtra("qr_email_to")}
                    Subject: ${intent.getStringExtra("qr_subject")}
                    Body: ${intent.getStringExtra("qr_body")}
                """.trimIndent()
                binding.tvTitle.text = "Email"
                binding.tvValue.text = info
            }
            "Phone" -> {
                binding.tvTitle.text = "Phone Number"
                binding.tvValue.text = intent.getStringExtra("qr_number")
            }
            "Text" -> {
                binding.tvTitle.text = "Text"
                binding.tvValue.text = intent.getStringExtra("qr_text")
            }
            else -> {
                binding.tvTitle.text = "QR Content"
                binding.tvValue.text = intent.getStringExtra("qr_raw")
            }
        }
    }
}