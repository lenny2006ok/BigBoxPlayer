package com.bigbox.player.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bigbox.player.data.remote.XtreamCategory
import com.bigbox.player.data.remote.XtreamStream
import com.bigbox.player.data.repository.XtreamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class MoviesUiState {
    object Idle : MoviesUiState()
    object Loading : MoviesUiState()
    data class SuccessCategories(val categories: List<XtreamCategory>) : MoviesUiState()
    data class SuccessStreams(val streams: List<XtreamStream>) : MoviesUiState()
    data class Error(val message: String) : MoviesUiState()
}

class MoviesViewModel(private val repository: XtreamRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<MoviesUiState>(MoviesUiState.Idle)
    val uiState: StateFlow<MoviesUiState> = _uiState

    var user: String = ""
    var pass: String = ""

    fun loadCategories(user: String, pass: String) {
        this.user = user
        this.pass = pass
        viewModelScope.launch {
            _uiState.value = MoviesUiState.Loading
            repository.getCategories(user, pass, "get_vod_categories")
                .onSuccess { _uiState.value = MoviesUiState.SuccessCategories(it) }
                .onFailure { _uiState.value = MoviesUiState.Error(it.message ?: "Erro ao carregar filmes") }
        }
    }

    fun loadStreams(catId: String) {
        viewModelScope.launch {
            _uiState.value = MoviesUiState.Loading
            repository.getStreams(user, pass, "get_vod_streams", catId)
                .onSuccess { _uiState.value = MoviesUiState.SuccessStreams(it) }
                .onFailure { _uiState.value = MoviesUiState.Error(it.message ?: "Erro ao carregar lista") }
        }
    }
}