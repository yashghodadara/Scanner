package com.example.scanner.presentetion

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.scanner.R
import com.example.scanner.data.QrHistoryItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter(
    private val list: MutableList<QrHistoryItem>,
    private val onItemClick: (QrHistoryItem) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {
    var deleteMode = false
    private val selectedIds = mutableSetOf<Long>()
    inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgType = itemView.findViewById<ImageView>(R.id.imgTypeIcon)
        val tvValue = itemView.findViewById<TextView>(R.id.tvValue)
        val tvType = itemView.findViewById<TextView>(R.id.tvType)
        val tvTime = itemView.findViewById<TextView>(R.id.tvTime)
        val checkBox = itemView.findViewById<CheckBox>(R.id.checkBoxItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.history_item, parent, false)
        return HistoryViewHolder(view)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = list[position]

        holder.tvValue.text = getDisplayValue(item)
        holder.tvType.text = item.qrType
        holder.tvTime.text = formatTime(item.timestamp)

        holder.imgType.setImageResource(getIconByType(item.qrType))

        holder.checkBox.visibility = if (deleteMode) View.VISIBLE else View.GONE
        holder.tvTime.visibility = if (deleteMode) View.GONE else View.VISIBLE

        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = selectedIds.contains(item.id)
        holder.checkBox.setOnCheckedChangeListener { _, checked ->
            if (checked) selectedIds.add(item.id)
            else selectedIds.remove(item.id)
        }

        holder.itemView.setOnClickListener {
            if (deleteMode) {
                val currentlySelected = selectedIds.contains(item.id)
                if (currentlySelected) {
                    selectedIds.remove(item.id)
                    holder.checkBox.isChecked = false
                } else {
                    selectedIds.add(item.id)
                    holder.checkBox.isChecked = true
                }
            } else {
                onItemClick(item)
            }
        }
    }
    private fun formatTime(timestamp: String): String {
        val timeMillis = timestamp.toLongOrNull() ?: return ""
        val date = Date(timeMillis)
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(date)
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
    fun enableDeleteMode(enable: Boolean) {
        deleteMode = enable
        if (enable) {
            selectedIds.clear()
        }
        notifyDataSetChanged()
    }

    fun getSelectedItems(): Set<Long> = selectedIds.toSet()
    fun removeByIds(ids: Set<Long>) {
        if (ids.isEmpty()) return
        list.removeAll { ids.contains(it.id) }
        selectedIds.removeAll(ids)
        deleteMode = false
        notifyDataSetChanged()
    }

    fun selectAll() {
        selectedIds.clear()
        list.forEach { selectedIds.add(it.id) }
        notifyDataSetChanged()
    }

    fun clearSelection() {
        selectedIds.clear()
        notifyDataSetChanged()
    }

    fun updateList(newList: List<QrHistoryItem>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
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
