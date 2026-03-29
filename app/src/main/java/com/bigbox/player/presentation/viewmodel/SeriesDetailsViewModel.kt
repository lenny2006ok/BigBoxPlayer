package com.bigbox.player.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bigbox.player.data.remote.XtreamCategory
import com.bigbox.player.data.remote.XtreamEpisode
import com.bigbox.player.data.remote.XtreamSeason
import com.bigbox.player.data.remote.XtreamSeriesInfo
import com.bigbox.player.data.repository.XtreamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class SeriesDetailsUiState {
    object Idle : SeriesDetailsUiState()
    object Loading : SeriesDetailsUiState()
    data class Success(
        val seasons: List<XtreamCategory>,
        val episodesMap: Map<String, List<XtreamEpisode>>,
        val initialEpisodes: List<XtreamEpisode>
    ) : SeriesDetailsUiState()
    data class Error(val message: String) : SeriesDetailsUiState()
}

class SeriesDetailsViewModel(private val repository: XtreamRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<SeriesDetailsUiState>(SeriesDetailsUiState.Idle)
    val uiState: StateFlow<SeriesDetailsUiState> = _uiState
    private val debugTag = "DEBUG_SERIES"

    fun loadSeriesDetails(user: String, pass: String, seriesId: Int) {
        viewModelScope.launch {
            Log.d(debugTag, "ViewModel: Solicitando detalhes para ID $seriesId")
            _uiState.value = SeriesDetailsUiState.Loading
            
            repository.getSeriesInfo(user, pass, seriesId)
                .onSuccess { originalInfo ->
                    val allEpisodes = originalInfo.episodes?.values?.flatten() ?: emptyList()
                    val seasonRegex = Regex("(?i)S(\\d+)")
                    
                    val regroupedMap = allEpisodes.groupBy { episode ->
                        val title = episode.title ?: ""
                        val match = seasonRegex.find(title)
                        match?.groupValues?.get(1)?.toIntOrNull()?.toString() ?: "1"
                    }

                    val generatedSeasons = regroupedMap.keys.map { seasonKey ->
                        val sNumber = seasonKey.toIntOrNull() ?: 1
                        XtreamCategory(seasonKey, "Temporada $seasonKey")
                    }.sortedBy { it.id.toIntOrNull() ?: 0 }

                    val firstSeasonEpisodes = if (generatedSeasons.isNotEmpty()) {
                        regroupedMap[generatedSeasons[0].id] ?: emptyList()
                    } else emptyList()

                    _uiState.value = SeriesDetailsUiState.Success(
                        seasons = generatedSeasons,
                        episodesMap = regroupedMap,
                        initialEpisodes = firstSeasonEpisodes
                    ) 
                }
                .onFailure { 
                    _uiState.value = SeriesDetailsUiState.Error(it.message ?: "Erro ao carregar detalhes") 
                }
        }
    }

    fun getStreamUrl(host: String, user: String, pass: String, episode: XtreamEpisode): String {
        return "$host/series/$user/$pass/${episode.id}.${episode.containerExtension ?: "mp4"}"
    }
}