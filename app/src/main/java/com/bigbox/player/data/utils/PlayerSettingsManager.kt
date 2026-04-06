package com.bigbox.player.data.utils

import android.content.Context
import android.content.SharedPreferences

class PlayerSettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("player_prefs", Context.MODE_PRIVATE)

    companion object {
        const val TYPE_EXO = "EXO"
        const val TYPE_VLC = "VLC"

        const val KEY_LIVE = "live_player"
        const val KEY_MOVIES = "movie_player"
        const val KEY_SERIES = "series_player"
    }

    fun getLivePlayer(): String = prefs.getString(KEY_LIVE, TYPE_EXO) ?: TYPE_EXO
    fun getMoviePlayer(): String = prefs.getString(KEY_MOVIES, TYPE_EXO) ?: TYPE_EXO
    fun getSeriesPlayer(): String = prefs.getString(KEY_SERIES, TYPE_EXO) ?: TYPE_EXO

    fun setLivePlayer(type: String) = prefs.edit().putString(KEY_LIVE, type).apply()
    fun setMoviePlayer(type: String) = prefs.edit().putString(KEY_MOVIES, type).apply()
    fun setSeriesPlayer(type: String) = prefs.edit().putString(KEY_SERIES, type).apply()

    /**
     * Salva a preferência do player baseado no tipo de conteúdo.
     * @param type Tipo de conteúdo (live, movie, series)
     * @param player O motor escolhido (EXO ou VLC)
     */
    fun savePlayer(type: String, player: String) {
        val key = when (type.lowercase()) {
            "live" -> KEY_LIVE
            "movie", "vod" -> KEY_MOVIES
            "series" -> KEY_SERIES
            else -> KEY_LIVE
        }
        prefs.edit().putString(key, player).apply()
    }
}
