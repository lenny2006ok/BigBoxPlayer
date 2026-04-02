package com.bigbox.player.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bigbox.player.data.remote.RetrofitClient
import com.bigbox.player.data.remote.TmdbMovieDetail
import com.bigbox.player.data.remote.XtreamVodResponse
import com.bigbox.player.data.repository.XtreamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class MovieDetailsUiState {
    object Idle : MovieDetailsUiState()
    object Loading : MovieDetailsUiState()
    data class Success(
        val vodInfo: XtreamVodResponse,
        val tmdbDetail: TmdbMovieDetail? = null
    ) : MovieDetailsUiState()
    data class Error(val message: String) : MovieDetailsUiState()
}

class MovieDetailsViewModel(private val repository: XtreamRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<MovieDetailsUiState>(MovieDetailsUiState.Idle)
    val uiState: StateFlow<MovieDetailsUiState> = _uiState

    fun loadMovieDetails(user: String, pass: String, vodId: Int) {
        viewModelScope.launch {
            _uiState.value = MovieDetailsUiState.Loading
            try {
                // 1. Buscar Info do VOD (IPTV)
                val response = repository.getVodInfo(user, pass, vodId)
                if (response.isSuccessful && response.body() != null) {
                    val vodData = response.body()!!
                    val tmdbId = vodData.info?.tmdbId

                    var tmdbDetail: TmdbMovieDetail? = null

                    // 2. Tentar buscar no TMDB se houver ID
                    if (!tmdbId.isNullOrEmpty() && tmdbId != "0") {
                        try {
                            val tmdbResponse = RetrofitClient.tmdbService.getMovieDetails(tmdbId)
                            if (tmdbResponse.isSuccessful) {
                                tmdbDetail = tmdbResponse.body()
                            }
                        } catch (e: Exception) {
                            // Se falhar o TMDB, continuamos com os dados do IPTV
                        }
                    }

                    _uiState.value = MovieDetailsUiState.Success(vodData, tmdbDetail)
                } else {
                    _uiState.value = MovieDetailsUiState.Error("Erro ao carregar detalhes do filme")
                }
            } catch (e: Exception) {
                _uiState.value = MovieDetailsUiState.Error("Erro de conexão: ${e.message}")
            }
        }
    }
}

class MovieViewModelFactory(private val repository: XtreamRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MovieDetailsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MovieDetailsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
