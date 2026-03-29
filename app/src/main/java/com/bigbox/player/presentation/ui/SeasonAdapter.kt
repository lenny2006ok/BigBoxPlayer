package com.bigbox.player.presentation.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bigbox.player.data.remote.XtreamCategory
import com.bigbox.player.databinding.ItemSeasonBinding

class SeasonAdapter(
    private val seasons: List<XtreamCategory>,
    private val onSeasonClick: (XtreamCategory) -> Unit
) : RecyclerView.Adapter<SeasonAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemSeasonBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSeasonBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val season = seasons[position]
        holder.binding.txtSeasonName.text = season.name
        
        holder.itemView.setOnClickListener {
            onSeasonClick(season)
        }
        
        // Feedback visual de foco já é tratado pelo background @drawable/bg_card_selector no XML
    }

    override fun getItemCount() = seasons.size
}