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
import coil.load
import com.bigbox.player.data.repository.XtreamRepository
import com.bigbox.player.databinding.ActivityMovieDetailsBinding
import com.bigbox.player.presentation.viewmodel.MovieDetailsUiState
import com.bigbox.player.presentation.viewmodel.MovieDetailsViewModel
import com.bigbox.player.presentation.viewmodel.MovieViewModelFactory
import kotlinx.coroutines.launch

class MovieDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMovieDetailsBinding

    private val viewModel: MovieDetailsViewModel by viewModels {
        val playlistUrl = intent.getStringExtra("PLAYLIST_URL") ?: ""
        val uri = Uri.parse(playlistUrl)
        val extractedHost = "${uri.scheme}://${uri.host}${if (uri.port != -1) ":${uri.port}" else ""}"
        MovieViewModelFactory(XtreamRepository(extractedHost))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMovieDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val vodId = intent.getIntExtra("STREAM_ID", -1)
        val playlistUrl = intent.getStringExtra("PLAYLIST_URL") ?: ""

        setupObservers()

        if (playlistUrl.isNotEmpty() && vodId != -1) {
            val uri = Uri.parse(playlistUrl)
            val user = uri.getQueryParameter("username") ?: ""
            val pass = uri.getQueryParameter("password") ?: ""
            viewModel.loadMovieDetails(user, pass, vodId)
        } else {
            finish()
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is MovieDetailsUiState.Loading -> binding.loadingDetails.visibility = View.VISIBLE
                        is MovieDetailsUiState.Success -> {
                            binding.loadingDetails.visibility = View.GONE
                            renderMovieDetails(state)
                        }
                        is MovieDetailsUiState.Error -> {
                            binding.loadingDetails.visibility = View.GONE
                            Toast.makeText(this@MovieDetailsActivity, state.message, Toast.LENGTH_SHORT).show()
                        }
                        else -> Unit
                    }
                }
            }
        }
    }

    private fun renderMovieDetails(state: MovieDetailsUiState.Success) {
        val info = state.vodInfo.info
        val movieData = state.vodInfo.movieData
        val tmdb = state.tmdbDetail

        // Prioridade: TMDB > Info do IPTV > Nome do Stream
        binding.txtMovieTitle.text = tmdb?.title ?: info?.name ?: movieData?.name ?: "Sem título"
        
        // Poster e Background
        val posterPath = if (tmdb != null) "https://image.tmdb.org/t/p/w500${tmdb.posterPath}" else info?.movieImage
        val backdropPath = if (tmdb != null) "https://image.tmdb.org/t/p/original${tmdb.backdropPath}" else info?.movieImage
        
        binding.imgMoviePoster.load(posterPath) {
            placeholder(com.bigbox.player.R.drawable.logo)
            error(com.bigbox.player.R.drawable.logo)
        }
        binding.imgMovieBackground.load(backdropPath) {
            placeholder(com.bigbox.player.R.drawable.logo)
            error(com.bigbox.player.R.drawable.logo)
        }

        // Detalhes Técnicos
        binding.txtMovieYear.text = tmdb?.releaseDate?.take(4) ?: info?.releaseDate?.take(4) ?: ""
        binding.txtMovieDuration.text = if (tmdb != null) "${tmdb.runtime} min" else "${info?.duration ?: "N/A"}"
        binding.txtMovieRating.text = "⭐ ${tmdb?.voteAverage ?: info?.rating ?: "N/A"}"
        
        // Gêneros
        val genres = tmdb?.genres?.joinToString(", ") { it.name } ?: info?.genre ?: ""
        binding.txtMovieGenres.text = genres

        // Sinopse
        binding.txtMoviePlot.text = tmdb?.overview ?: info?.plot ?: "Nenhuma sinopse disponível."

        // Elenco e Diretor
        binding.txtMovieCast.text = "Elenco: ${info?.actors ?: "N/A"}"
        binding.txtMovieDirector.text = "Diretor: ${info?.director ?: "N/A"}"

        // Botão Play
        binding.btnPlayMovie.setOnClickListener {
            val playlistUrl = intent.getStringExtra("PLAYLIST_URL") ?: ""
            startActivity(Intent(this, PlayerActivity::class.java).apply {
                putExtra("PLAYLIST_URL", playlistUrl)
                putExtra("STREAM_ID", movieData?.streamId.toString())
                putExtra("STREAM_TYPE", "movie")
                putExtra("CONTAINER_EXTENSION", movieData?.containerExtension)
                putExtra("STREAM_NAME", movieData?.name)
            })
        }
    }
}
