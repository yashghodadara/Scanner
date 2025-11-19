package com.example.scanner.presentetion

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.scanner.R
import com.example.scanner.data.QrHistoryItem

class FavoritesAdapter(
    private val list: MutableList<QrHistoryItem>,
    private val onItemClick: (QrHistoryItem) -> Unit
) : RecyclerView.Adapter<FavoritesAdapter.HistoryViewHolder>() {
    inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgType = itemView.findViewById<ImageView>(R.id.imgTypeIcon)
        val tvValue = itemView.findViewById<TextView>(R.id.tvValue)
        val tvType = itemView.findViewById<TextView>(R.id.tvType)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.favorite_item, parent, false)
        return HistoryViewHolder(view)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = list[position]

        holder.tvValue.text = getDisplayValue(item)
        holder.tvType.text = item.qrType

        holder.imgType.setImageResource(getIconByType(item.qrType))

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }
    private fun getIconByType(type: String): Int {
        return when (type) {
            "URL" -> R.drawable.ic_icon_url_blue
            "Text" -> R.drawable.ic_icon_text_blue
            "Phone" -> R.drawable.ic_icon_phone_blue
            "Email" -> R.drawable.ic_icon_email_blue
            "Contact" -> R.drawable.ic_icon_contact_blue
            "WiFi" -> R.drawable.ic_icon_wifi_blue
            else -> R.drawable.ic_icon_text_blue
        }
    }

    private fun getDisplayValue(item: QrHistoryItem): String {
        return when (item.qrType) {

            "Phone" -> {
                extractAfterColon(item.value)
            }

            "URL" -> extractAfterColon(item.value)

            "Email" -> extractAfterColon(item.value)

            "Text" -> extractAfterColon(item.value)

            "WiFi" -> {
                try {
                    val json = org.json.JSONObject(item.jsonData)
                    json.optString("ssid", "")
                } catch (e: Exception) {
                    extractAfterColon(item.value)
                }
            }

            "Contact" -> {
                try {
                    val json = org.json.JSONObject(item.jsonData)
                    json.optString("name", "")
                } catch (e: Exception) {
                    extractAfterColon(item.value)
                }
            }

            else -> extractAfterColon(item.value)
        }
    }

    private fun extractAfterColon(text: String): String {
        return if (text.contains(":")) {
            text.substringAfter(":")
        } else text
    }

}
