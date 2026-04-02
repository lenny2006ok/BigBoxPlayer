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

sealed class SeriesUiState {
    object Idle : SeriesUiState()
    object Loading : SeriesUiState()
    data class SuccessCategories(val categories: List<XtreamCategory>) : SeriesUiState()
    data class SuccessStreams(val streams: List<XtreamStream>) : SeriesUiState()
    data class Error(val message: String) : SeriesUiState()
}

class SeriesViewModel(private val repository: XtreamRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<SeriesUiState>(SeriesUiState.Idle)
    val uiState: StateFlow<SeriesUiState> = _uiState

    private var loadingJob: Job? = null
    var user: String = ""
    var pass: String = ""

    fun loadCategories(user: String, pass: String) {
        this.user = user
        this.pass = pass
        viewModelScope.launch {
            _uiState.value = SeriesUiState.Loading
            repository.getCategories(user, pass, "get_series_categories")
                .onSuccess { _uiState.value = SeriesUiState.SuccessCategories(it) }
                .onFailure { _uiState.value = SeriesUiState.Error(it.message ?: "Erro ao carregar séries") }
        }
    }

    fun loadStreams(catId: String) {
        loadingJob?.cancel()
        loadingJob = viewModelScope.launch {
            _uiState.value = SeriesUiState.Loading
            repository.getStreams(user, pass, "get_series", catId)
                .onSuccess { _uiState.value = SeriesUiState.SuccessStreams(it) }
                .onFailure { _uiState.value = SeriesUiState.Error(it.message ?: "Erro ao carregar lista") }
        }
    }
}