package com.bigbox.player.presentation.player

import android.content.Context
import android.net.Uri
import android.widget.FrameLayout
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.VLCVideoLayout

/**
 * Implementação do motor de vídeo utilizando LibVLC.
 * Otimizado para baixa latência em fluxos de rede (IPTV).
 */
class VlcPlayerEngine : VideoEngine {

    private var libVLC: LibVLC? = null
    private var mediaPlayer: MediaPlayer? = null
    private var videoLayout: VLCVideoLayout? = null

    override fun init(context: Context, container: FrameLayout) {
        // 1. Configurar opções da LibVLC (Caching de rede é vital para IPTV)
        val options = arrayListOf(
            "--network-caching=3000",      // Buffer de rede de 3 segundos
            "--clock-jitter=0",            // Reduz jitter de sincronia
            "--clock-synchro=0",           // Sincronia de clock agressiva
            "--drop-late-frames",          // Descarta frames atrasados para manter tempo real
            "--skip-frames"                // Pula frames se necessário
        )

        libVLC = LibVLC(context, options)
        mediaPlayer = MediaPlayer(libVLC)

        // 2. Criar o layout de vídeo do VLC programaticamente
        videoLayout = VLCVideoLayout(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        // 3. Injetar no container e conectar as views
        container.removeAllViews()
        container.addView(videoLayout)
        mediaPlayer?.attachViews(videoLayout!!, null, true, false)
    }

    override fun playUrl(url: String) {
        if (libVLC == null || mediaPlayer == null) return

        val media = Media(libVLC, Uri.parse(url)).apply {
            // Habilita aceleração de hardware para melhor performance
            setHWDecoderEnabled(true, false)
            addOption(":network-caching=3000")
        }

        mediaPlayer?.media = media
        mediaPlayer?.play()
        
        // Media deve ser liberado após ser atribuído ao MediaPlayer
        media.release()
    }

    override fun pause() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
        }
    }

    override fun resume() {
        mediaPlayer?.play()
    }

    override fun stop() {
        mediaPlayer?.stop()
    }

    override fun release() {
        mediaPlayer?.let {
            it.stop()
            it.detachViews()
            it.release()
        }
        libVLC?.release()
        
        videoLayout = null
        mediaPlayer = null
        libVLC = null
    }
}
