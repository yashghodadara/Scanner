package com.example.scanner.presentetion

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scanner.R
import com.example.scanner.data.QrAllHistoryDatabase
import com.example.scanner.data.QrHistoryItem
import com.example.scanner.databinding.ActivityFavoriteHistoryBinding
import com.example.scanner.util.BaseActivity
import org.json.JSONObject

class FavoriteHistoryActivity : BaseActivity() {
    private lateinit var binding : ActivityFavoriteHistoryBinding
    lateinit var db: QrAllHistoryDatabase
    lateinit var favoritesAdapter: FavoritesAdapter
    val favoriteList = mutableListOf<QrHistoryItem>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding  = ActivityFavoriteHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        db = QrAllHistoryDatabase(this)
        val list = db.getAllQrData().toMutableList()
        favoriteList.clear()
        favoriteList.addAll(list.filter { it.isFavorite })

        favoritesAdapter = FavoritesAdapter(favoriteList) { clickedItem ->
            onHistoryItemClick(clickedItem)
        }
        binding.rlHistory.layoutManager = LinearLayoutManager(this)
        binding.rlHistory.adapter = favoritesAdapter

        binding.ivBack.setOnClickListener {
            finish()
        }
    }
    private fun onHistoryItemClick(item: QrHistoryItem) {
        val json = JSONObject(item.jsonData)
        when (item.qrType) {
            "URL" -> {
                val intent = Intent(this, QRUrlDetailsActivity::class.java)
                intent.putExtra("qr_history_id", item.id)
                intent.putExtra("qr_url", item.value)
                intent.putExtra("qr_image", item.qrImage)
                intent.putExtra("qr_type",item.qrType)
                startActivity(intent)
            }

            "Text" -> {
                val intent = Intent(this, QRTextContactActivity::class.java)
                intent.putExtra("qr_history_id", item.id)
                intent.putExtra("qr_text", item.value)
                intent.putExtra("qr_image", item.qrImage)
                intent.putExtra("qr_type",item.qrType)
                startActivity(intent)
            }

            "Email" -> {
                val intent = Intent(this, QRPhoneEmailDetailsActivity::class.java)
                intent.putExtra("qr_history_id", item.id)
                intent.putExtra("qr_type", item.value)
                intent.putExtra("qr_image", item.qrImage)
                intent.putExtra("qr_email_to",json.optString("address"))
                intent.putExtra("qr_subject", json.optString("subject"))
                intent.putExtra("qr_body", json.optString("body"))
                startActivity(intent)
            }

            "Phone" -> {
                val intent = Intent(this, QRPhoneEmailDetailsActivity::class.java)
                intent.putExtra("qr_history_id", item.id)
                intent.putExtra("qr_type", item.qrType)
                intent.putExtra("qr_image", item.qrImage)
                intent.putExtra("qr_number", json.optString("number"))
                startActivity(intent)
            }

            "Contact" -> {
                val intent = Intent(this, QRTextContactActivity::class.java)
                intent.putExtra("qr_history_id", item.id)
                intent.putExtra("qr_type", item.qrType)
                intent.putExtra("qr_image", item.qrImage)
                intent.putExtra("qr_name", json.optString("name"))
                intent.putExtra("qr_organization", json.optString("organization"))
                intent.putExtra("qr_title", json.optString("title"))
                intent.putExtra("qr_address", json.optString("address"))
                intent.putExtra("qr_phone", json.optString("phone"))
                intent.putExtra("qr_email", json.optString("email"))
                startActivity(intent)
            }

            "WiFi" -> {
                val intent = Intent(this, QRUrlDetailsActivity::class.java)
                intent.putExtra("qr_history_id", item.id)
                intent.putExtra("qr_type", item.qrType)
                intent.putExtra("qr_image", item.qrImage)
                intent.putExtra("qr_ssid", json.optString("ssid"))
                intent.putExtra("qr_password", json.optString("password"))
                intent.putExtra("qr_encryptionType", json.optString("encryptionType"))
                startActivity(intent)
            }

            else -> {
                val intent = Intent(this, QRUrlDetailsActivity::class.java)
                intent.putExtra("qr_raw", json.optString("rawValue"))
                intent.putExtra("qr_type", item.qrType)
                intent.putExtra("qr_image", item.qrImage)
                startActivity(intent)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        // Reload DB data
        val updatedList = db.getAllQrData().filter { it.isFavorite }

        favoriteList.clear()
        favoriteList.addAll(updatedList)

        favoritesAdapter.notifyDataSetChanged()
    }
}