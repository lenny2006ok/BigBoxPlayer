package com.bigbox.player.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bigbox.player.data.remote.XtreamCategory
import com.bigbox.player.data.remote.XtreamStream
import com.bigbox.player.data.repository.XtreamRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class LiveUiState {
    object Idle : LiveUiState()
    object Loading : LiveUiState()
    data class SuccessCategories(val categories: List<XtreamCategory>) : LiveUiState()
    data class SuccessStreams(val streams: List<XtreamStream>) : LiveUiState()
    data class Error(val message: String) : LiveUiState()
}

class LiveViewModel(private val repository: XtreamRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<LiveUiState>(LiveUiState.Idle)
    val uiState: StateFlow<LiveUiState> = _uiState

    private var loadingJob: Job? = null
    var user: String = ""
    var pass: String = ""

    fun loadCategories(user: String, pass: String) {
        this.user = user
        this.pass = pass
        viewModelScope.launch {
            _uiState.value = LiveUiState.Loading
            repository.getCategories(user, pass, "get_live_categories")
                .onSuccess { _uiState.value = LiveUiState.SuccessCategories(it) }
                .onFailure { _uiState.value = LiveUiState.Error(it.message ?: "Erro desconhecido") }
        }
    }

    fun loadStreams(catId: String) {
        loadingJob?.cancel()
        loadingJob = viewModelScope.launch {
            _uiState.value = LiveUiState.Loading
            repository.getStreams(user, pass, "get_live_streams", catId)
                .onSuccess { _uiState.value = LiveUiState.SuccessStreams(it) }
                .onFailure { _uiState.value = LiveUiState.Error(it.message ?: "Erro desconhecido") }
        }
    }
}