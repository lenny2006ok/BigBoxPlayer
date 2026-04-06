package com.bigbox.player.presentation.player

import android.content.Context
import com.bigbox.player.data.utils.PlayerSettingsManager

class PlayerFactory(private val settingsManager: PlayerSettingsManager) {

    /**
     * Cria o motor de vídeo baseado na configuração do usuário.
     * @param context Contexto da aplicação
     * @param contentType Tipo de conteúdo (live, movie, series)
     * @return Implementação de VideoEngine (Exo ou VLC)
     */
    fun createEngine(context: Context, contentType: String): VideoEngine {
        val playerType = when (contentType.lowercase()) {
            "live" -> settingsManager.getLivePlayer()
            "movie", "vod" -> settingsManager.getMoviePlayer()
            "series" -> settingsManager.getSeriesPlayer()
            else -> settingsManager.getLivePlayer()
        }

        return if (playerType == PlayerSettingsManager.TYPE_VLC) {
            VlcPlayerEngine()
        } else {
            ExoPlayerEngine()
        }
    }
}
