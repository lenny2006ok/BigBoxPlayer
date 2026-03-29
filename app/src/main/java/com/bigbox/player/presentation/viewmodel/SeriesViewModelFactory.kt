package com.bigbox.player.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bigbox.player.data.repository.XtreamRepository

class SeriesViewModelFactory(private val repository: XtreamRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(SeriesViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                SeriesViewModel(repository) as T
            }
            modelClass.isAssignableFrom(SeriesDetailsViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                SeriesDetailsViewModel(repository) as T
            }
            modelClass.isAssignableFrom(PlayerViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                PlayerViewModel(repository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}