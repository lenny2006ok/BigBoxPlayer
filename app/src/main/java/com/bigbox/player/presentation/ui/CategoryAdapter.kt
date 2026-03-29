package com.bigbox.player.presentation.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bigbox.player.data.remote.XtreamCategory
import com.bigbox.player.databinding.ItemStreamBinding

class CategoryAdapter(
    private val categories: List<XtreamCategory>,
    private val onCategoryClick: (XtreamCategory) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemStreamBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemStreamBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cat = categories[position]
        holder.binding.txtStreamName.text = cat.name
        holder.binding.imgStreamIcon.setImageResource(android.R.drawable.ic_menu_slideshow)
        holder.itemView.setOnClickListener { onCategoryClick(cat) }
    }

    override fun getItemCount() = categories.size
}