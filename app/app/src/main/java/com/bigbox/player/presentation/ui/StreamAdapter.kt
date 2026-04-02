package com.bigbox.player.presentation.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.request.CachePolicy
import com.bigbox.player.data.remote.XtreamStream
import com.bigbox.player.databinding.ItemStreamBinding
import androidx.constraintlayout.widget.ConstraintLayout
import android.view.ViewGroup.MarginLayoutParams

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
            crossfade(100) // Reduzido para maior fluidez
            placeholder(com.bigbox.player.R.drawable.logo)
            error(com.bigbox.player.R.drawable.logo)
            memoryCachePolicy(CachePolicy.ENABLED)
            diskCachePolicy(CachePolicy.ENABLED)
        }

        // Animação de Foco (Otimização para TV)
        holder.itemView.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                // Aumenta escala e margin do título
                view.animate().scaleX(1.1f).scaleY(1.1f).setDuration(200).start()
                val params = holder.binding.txtStreamName.layoutParams as MarginLayoutParams
                params.topMargin = 16 // Aumenta margin no foco
                holder.binding.txtStreamName.layoutParams = params
            } else {
                // Volta ao normal
                view.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start()
                val params = holder.binding.txtStreamName.layoutParams as MarginLayoutParams
                params.topMargin = 8 // Volta margin padrão
                holder.binding.txtStreamName.layoutParams = params
            }
        }

        holder.itemView.setOnClickListener {
            onItemClick(stream)
        }
    }

    override fun getItemCount() = items.size
}