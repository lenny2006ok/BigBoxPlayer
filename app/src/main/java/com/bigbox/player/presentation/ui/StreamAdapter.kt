package com.bigbox.player.presentation.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.bigbox.player.data.remote.XtreamStream
import com.bigbox.player.databinding.ItemStreamBinding

class StreamAdapter(
    private val items: List<XtreamStream>,
    private val onItemClick: (XtreamStream) -> Unit
) : RecyclerView.Adapter<StreamAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemStreamBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemStreamBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val stream = items[position]
        holder.binding.txtStreamName.text = stream.name
        
        // Manobra Sênior: Séries usam 'cover', Canais/Filmes usam 'icon'
        val imageUrl = if (!stream.cover.isNullOrEmpty()) stream.cover else stream.icon
        
        holder.binding.imgStreamIcon.load(imageUrl) {
            crossfade(true)
            placeholder(android.R.drawable.ic_menu_report_image)
            error(android.R.drawable.ic_menu_report_image)
        }

        holder.itemView.setOnClickListener {
            onItemClick(stream)
        }
    }

    override fun getItemCount() = items.size
}