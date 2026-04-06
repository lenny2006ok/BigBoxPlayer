package com.bigbox.player.presentation.player

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource

class ExoPlayerEngine : VideoEngine {
    private var player: ExoPlayer? = null
    private var playerView: PlayerView? = null

    override fun init(context: Context, container: FrameLayout) {
        Log.e("DEBUG_PLAYER", "=> Motor Selecionado: EXO PLAYER")

        // Disfarce de Rede para passar no bloqueio do servidor IPTV
        val userAgent = android.webkit.WebSettings.getDefaultUserAgent(context)
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent(userAgent)
            .setAllowCrossProtocolRedirects(true)

        val mediaSourceFactory = DefaultMediaSourceFactory(httpDataSourceFactory)

        player = ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()

        playerView = PlayerView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            this.player = this@ExoPlayerEngine.player
            this.useController = true
        }

        container.removeAllViews()
        container.addView(playerView)

        // O nosso "Dedo Duro" Sênior
        player?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when(playbackState) {
                    Player.STATE_BUFFERING -> Log.e("DEBUG_PLAYER", "ExoPlayer: Carregando vídeo (Buffering)...")
                    Player.STATE_READY -> Log.e("DEBUG_PLAYER", "ExoPlayer: Pronto para tocar!")
                    Player.STATE_ENDED -> Log.e("DEBUG_PLAYER", "ExoPlayer: Vídeo terminou.")
                    Player.STATE_IDLE -> Log.e("DEBUG_PLAYER", "ExoPlayer: Parado (Idle).")
                }
            }
            override fun onPlayerError(error: PlaybackException) {
                Log.e("DEBUG_PLAYER", "ExoPlayer MORREU: ${error.message} | Código: ${error.errorCodeName}")
            }
        })
    }

    override fun playUrl(url: String) {
        Log.e("DEBUG_PLAYER", "ExoPlayer: Tentando dar o play na URL...")
        player?.setMediaItem(MediaItem.fromUri(Uri.parse(url)))
        player?.prepare()
        player?.playWhenReady = true
    }

    override fun pause() { player?.pause() }
    override fun resume() { player?.play() }
    override fun stop() { player?.stop() }

    override fun release() {
        player?.release()
        player = null
        playerView = null
    }
}