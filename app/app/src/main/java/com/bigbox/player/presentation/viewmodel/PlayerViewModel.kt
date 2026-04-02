package com.bigbox.player.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bigbox.player.data.repository.XtreamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PlayerViewModel(private val repository: XtreamRepository) : ViewModel() {

    private val _streamUrl = MutableStateFlow<String>("")
    val streamUrl: StateFlow<String> = _streamUrl

    fun prepareStream(id: String, type: String, extension: String?) {
        viewModelScope.launch {
            val url = repository.buildStreamUrl(id, type, extension)
            _streamUrl.value = url
        }
    }
}