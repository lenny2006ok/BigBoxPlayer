package com.bigbox.player.presentation.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bigbox.player.data.remote.XtreamCategory
import com.bigbox.player.databinding.ItemSidebarCategoryBinding
import kotlinx.coroutines.*

class CategoryAdapter(
    private val categories: List<XtreamCategory>,
    private val onCategorySelected: (XtreamCategory) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    private var selectedPosition = 0
    private val adapterScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var selectionJob: Job? = null

    class ViewHolder(val binding: ItemSidebarCategoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSidebarCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cat = categories[position]
        holder.binding.txtCategoryName.text = cat.name
        
        // Estado Visual: Seleção (Sincronizado com o Grid)
        holder.itemView.isSelected = (position == selectedPosition)

        // Comportamento focado no D-Pad (Controle Remoto)
        holder.itemView.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                updateSelected(position)
                
                // Debounce Inteligente: Só carrega se o foco parar por 350ms
                selectionJob?.cancel()
                selectionJob = adapterScope.launch {
                    delay(350)
                    onCategorySelected(cat)
                }
            }
        }

        holder.itemView.setOnClickListener {
            selectionJob?.cancel()
            updateSelected(position)
            onCategorySelected(cat)
        }
    }

    private fun updateSelected(newPos: Int) {
        val oldPos = selectedPosition
        selectedPosition = newPos
        notifyItemChanged(oldPos)
        notifyItemChanged(selectedPosition)
    }

    override fun getItemCount() = categories.size
    
    // Limpar recursos ao destruir
    fun onDetached() {
        adapterScope.cancel()
    }
}