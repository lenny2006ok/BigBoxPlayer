package com.bigbox.player.presentation.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.bigbox.player.data.remote.XtreamEpisode
import com.bigbox.player.databinding.ItemEpisodeBinding

class EpisodeAdapter(
    private val episodes: List<XtreamEpisode>,
    private val onEpisodeClick: (XtreamEpisode) -> Unit
) : RecyclerView.Adapter<EpisodeAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemEpisodeBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEpisodeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val episode = episodes[position]
        holder.binding.txtEpisodeTitle.text = "E${episode.episodeNumber} - ${episode.title}"
        holder.binding.txtEpisodeInfo.text = episode.info?.plot ?: "Sem descrição disponível."
        
        holder.binding.imgEpisodeIcon.load(episode.info?.movieImage) {
            crossfade(true)
            placeholder(com.bigbox.player.R.drawable.logo)
            error(com.bigbox.player.R.drawable.logo)
        }

        holder.itemView.setOnClickListener {
            onEpisodeClick(episode)
        }
    }

    override fun getItemCount() = episodes.size
}