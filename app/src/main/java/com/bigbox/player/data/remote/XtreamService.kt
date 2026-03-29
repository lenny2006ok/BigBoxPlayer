package com.bigbox.player.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface XtreamService {
    // Pegar Categorias (Canais, Filmes ou Séries)
    @GET("player_api.php")
    suspend fun getCategories(
        @Query("username") u: String,
        @Query("password") p: String,
        @Query("action") action: String // get_live_categories, get_vod_categories, get_series_categories
    ): Response<List<XtreamCategory>>

    // Pegar Streams por Categoria
    @GET("player_api.php")
    suspend fun getStreams(
        @Query("username") u: String,
        @Query("password") p: String,
        @Query("action") action: String, // get_live_streams, get_vod_streams, get_series
        @Query("category_id") cid: String
    ): Response<List<XtreamStream>>

    // Pegar Informações da Série (Temporadas e Episódios)
    @GET("player_api.php")
    suspend fun getSeriesInfo(
        @Query("username") u: String,
        @Query("password") p: String,
        @Query("action") action: String = "get_series_info",
        @Query("series_id") seriesId: Int
    ): Response<XtreamSeriesInfo>
}