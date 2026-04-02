package com.bigbox.player.presentation.ui

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bigbox.player.data.repository.XtreamRepository
import com.bigbox.player.databinding.ActivityPlayerBinding
import com.bigbox.player.presentation.viewmodel.PlayerViewModel
import com.bigbox.player.presentation.viewmodel.SeriesViewModelFactory
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import kotlinx.coroutines.launch

class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private var player: ExoPlayer? = null

    private val viewModel: PlayerViewModel by viewModels {
        val playlistUrl = intent.getStringExtra("PLAYLIST_URL") ?: ""
        val uri = Uri.parse(playlistUrl)
        val host = "${uri.scheme}://${uri.host}${if (uri.port != -1) ":${uri.port}" else ""}"
        val user = uri.getQueryParameter("username") ?: ""
        val pass = uri.getQueryParameter("password") ?: ""
        SeriesViewModelFactory(XtreamRepository(host, user, pass))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val streamId = intent.getStringExtra("STREAM_ID") ?: ""
        val type = intent.getStringExtra("STREAM_TYPE") ?: "live"
        val extension = intent.getStringExtra("CONTAINER_EXTENSION")

        setupObservers()
        viewModel.prepareStream(streamId, type, extension)
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.streamUrl.collect { url ->
                    if (url.isNotEmpty()) {
                        Log.e("DEBUG_PLAYER", "Iniciando Player com URL: $url")
                        initializePlayer(url)
                    }
                }
            }
        }
    }

    private fun initializePlayer(url: String) {
        // 1. Captura Dinâmica do User-Agent do Dispositivo
        val userAgent = android.webkit.WebSettings.getDefaultUserAgent(this)

        // 2. Configuração da Factory de Rede Robusta
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent(userAgent)
            .setAllowCrossProtocolRedirects(true)

        // 3. Vincular ao MediaSource do Player (Correção para versão 2.17.1)
        // O construtor correto para injetar a factory de dados é passar apenas ela ou (context, extractorFactory)
        val mediaSourceFactory = DefaultMediaSourceFactory(httpDataSourceFactory)

        player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(mediaSourceFactory)
            .build().apply {
                binding.playerView.player = this
                val mediaItem = MediaItem.fromUri(url)
                setMediaItem(mediaItem)
                prepare()
                playWhenReady = true
                
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        binding.txtPlayerStatus.visibility = if (state == Player.STATE_BUFFERING) View.VISIBLE else View.GONE
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        super.onPlayerError(error)
                        Log.e("DEBUG_PLAYER", "ExoPlayer Error: ${error.message} | Code: ${error.errorCodeName}")
                        Toast.makeText(this@PlayerActivity, "Erro: ${error.errorCodeName}", Toast.LENGTH_LONG).show()
                    }
                })
            }
    }

    override fun onStop() {
        super.onStop()
        player?.release()
        player = null
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
    }
}