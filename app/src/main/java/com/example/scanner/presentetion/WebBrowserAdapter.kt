package com.example.scanner.presentetion

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.scanner.R
import com.example.scanner.data.BrowserItem

class WebBrowserAdapter(
    private val browserList: List<BrowserItem>,
    private var selectedBrowser: String,
    private val onClick: (BrowserItem) -> Unit
) : RecyclerView.Adapter<WebBrowserAdapter.BrowserViewHolder>() {

    class BrowserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvBrowserName: TextView = itemView.findViewById(R.id.tvBrowserName)
        val ivBrowser: ImageView = itemView.findViewById(R.id.ivBrowser)
        val container: LinearLayout = itemView as LinearLayout
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrowserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_browser, parent, false)
        return BrowserViewHolder(view)
    }

    override fun onBindViewHolder(holder: BrowserViewHolder, position: Int) {
        val item = browserList[position]

        holder.tvBrowserName.text = item.name
        holder.ivBrowser.setImageResource(item.iconRes)

        val isSelected = item.name == selectedBrowser
        holder.container.isSelected = isSelected

        holder.itemView.setOnClickListener {
            selectedBrowser = item.name
            notifyDataSetChanged()
            onClick(item)
        }
    }

    override fun getItemCount(): Int = browserList.size
}
