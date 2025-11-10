package com.example.scanner.language

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.scanner.R
import com.google.android.material.textview.MaterialTextView
import kotlin.collections.indexOfFirst
import kotlin.text.equals

class LanguageAdapter(
    private val languagesList: MutableList<Language>,
    private var selectedLanguageName: String,
    private var selectedCode: String,
    private val onSelect: (Language) -> Unit
) : RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder>() {
    inner class LanguageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: MaterialTextView = itemView.findViewById(R.id.tvLanguageName)
        val tvEnglishName: MaterialTextView = itemView.findViewById(R.id.tvLanguageEnglishName)
        val imgFlag: AppCompatImageView = itemView.findViewById(R.id.imgFlag)
        val llMain: LinearLayout = itemView.findViewById(R.id.llMain)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_language, parent, false)
        return LanguageViewHolder(view)
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
        val language = languagesList[position]
        holder.tvName.text = language.name
        holder.tvEnglishName.text = "(" +language.languageName +")"
        holder.imgFlag.setImageResource(language.flagIcon)


        val isSelected = selectedCode.equals(language.code, ignoreCase = true)
        holder.llMain.isSelected = isSelected
        val previous = selectedCode
        if (isSelected) {
            holder.tvEnglishName.setTextColor(Color.parseColor("#FFFFFF"))
        }

        holder.itemView.setOnClickListener {
            selectedLanguageName = language.languageName
            selectedCode = language.code
            notifyDataSetChanged()
            onSelect(language)
            notifyItemChanged(languagesList.indexOfFirst { it.code == previous })
            notifyItemChanged(position)
        }
    }

    override fun getItemCount() = languagesList.size
}