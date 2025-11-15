package com.example.scanner.presentetion

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.scanner.R

class SearchEngineAdapter(
    private val searchEngines: List<String>,
    private var selectedEngine: String,
    private val onEngineSelected: (String) -> Unit
) : RecyclerView.Adapter<SearchEngineAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvEngineName: TextView = itemView.findViewById(R.id.tvEngineName)
        val rbSelected: RadioButton = itemView.findViewById(R.id.rbSelected)
        val container: LinearLayout = itemView.findViewById(R.id.container)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_engine, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val engine = searchEngines[position]

        holder.tvEngineName.text = engine
        holder.rbSelected.isChecked = engine == selectedEngine

        holder.container.setOnClickListener {
            selectedEngine = engine
            notifyDataSetChanged()
            onEngineSelected(engine)
        }

        holder.rbSelected.setOnClickListener {
            selectedEngine = engine
            notifyDataSetChanged()
            onEngineSelected(engine)
        }
    }

    override fun getItemCount(): Int = searchEngines.size
}