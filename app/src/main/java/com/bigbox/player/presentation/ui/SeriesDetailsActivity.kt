package com.bigbox.player.presentation.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.bigbox.player.data.remote.XtreamEpisode
import com.bigbox.player.data.repository.XtreamRepository
import com.bigbox.player.databinding.ActivitySeriesDetailsBinding
import com.bigbox.player.presentation.viewmodel.SeriesDetailsUiState
import com.bigbox.player.presentation.viewmodel.SeriesDetailsViewModel
import com.bigbox.player.presentation.viewmodel.SeriesViewModelFactory
import kotlinx.coroutines.launch

class SeriesDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySeriesDetailsBinding

    private val viewModel: SeriesDetailsViewModel by viewModels {
        val playlistUrl = intent.getStringExtra("PLAYLIST_URL") ?: ""
        val uri = Uri.parse(playlistUrl)
        val extractedHost = "${uri.scheme}://${uri.host}${if (uri.port != -1) ":${uri.port}" else ""}"
        SeriesViewModelFactory(XtreamRepository(extractedHost))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeriesDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val seriesId = intent.getIntExtra("SERIES_ID", -1)
        val seriesName = intent.getStringExtra("SERIES_NAME") ?: ""
        val posterUrl = intent.getStringExtra("SERIES_POSTER") ?: ""
        val playlistUrl = intent.getStringExtra("PLAYLIST_URL") ?: ""

        binding.txtSeriesTitle.text = seriesName
        binding.imgSeriesPoster.load(posterUrl)
        binding.imgSeriesBackground.load(posterUrl)

        setupObservers()

        if (playlistUrl.isNotEmpty() && seriesId != -1) {
            val uri = Uri.parse(playlistUrl)
            val user = uri.getQueryParameter("username") ?: ""
            val pass = uri.getQueryParameter("password") ?: ""
            viewModel.loadSeriesDetails(user, pass, seriesId)
        } else {
            finish()
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is SeriesDetailsUiState.Loading -> binding.loadingDetails.visibility = View.VISIBLE
                        is SeriesDetailsUiState.Success -> {
                            binding.loadingDetails.visibility = View.GONE
                            renderSeasons(state)
                        }
                        is SeriesDetailsUiState.Error -> {
                            binding.loadingDetails.visibility = View.GONE
                            Toast.makeText(this@SeriesDetailsActivity, state.message, Toast.LENGTH_SHORT).show()
                        }
                        else -> Unit
                    }
                }
            }
        }
    }

    private fun renderSeasons(state: SeriesDetailsUiState.Success) {
        binding.recyclerSeasons.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerSeasons.adapter = SeasonAdapter(state.seasons) { selectedSeason ->
            renderEpisodes(state.episodesMap[selectedSeason.id] ?: emptyList())
        }
        renderEpisodes(state.initialEpisodes)
    }

    private fun renderEpisodes(episodes: List<XtreamEpisode>) {
        binding.recyclerEpisodes.layoutManager = LinearLayoutManager(this)
        binding.recyclerEpisodes.adapter = EpisodeAdapter(episodes) { ep ->
            // SINCRONIZAÇÃO DE ORIGEM (PASSO 1)
            val playlistUrl = intent.getStringExtra("PLAYLIST_URL") ?: ""
            startActivity(Intent(this, PlayerActivity::class.java).apply {
                putExtra("PLAYLIST_URL", playlistUrl)
                putExtra("STREAM_ID", ep.id)
                putExtra("STREAM_TYPE", "series") // Forçando rota de séries
                putExtra("CONTAINER_EXTENSION", ep.containerExtension)
                putExtra("STREAM_NAME", ep.title)
            })
        }
    }
}