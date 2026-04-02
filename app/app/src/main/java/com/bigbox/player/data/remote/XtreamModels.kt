package com.bigbox.player.data.remote

import com.google.gson.annotations.SerializedName

data class XtreamCategory(
    @SerializedName("category_id") val id: String,
    @SerializedName("category_name") val name: String
)

data class XtreamStream(
    @SerializedName("num") val num: Int,
    @SerializedName("name") val name: String,
    @SerializedName("stream_type") val streamType: String,
    @SerializedName("stream_id") val streamId: Int,
    @SerializedName("series_id") val seriesId: Int?,
    @SerializedName("stream_icon") val icon: String?,
    @SerializedName("cover") val cover: String?,
    @SerializedName("category_id") val categoryId: String?,
    @SerializedName("container_extension") val containerExtension: String? // CORREÇÃO: Mapeamento dinâmico da extensão para VOD
)

data class XtreamSeriesInfo(
    @SerializedName("seasons") val seasons: List<XtreamSeason>?,
    @SerializedName("episodes") val episodes: Map<String, List<XtreamEpisode>>?
)

data class XtreamSeason(
    @SerializedName("season_number") val seasonNumber: Int?,
    @SerializedName("name") val name: String?,
    @SerializedName("episode_count") val episodeCount: Int?
)

data class XtreamEpisode(
    @SerializedName("id") val id: String?,
    @SerializedName("episode_num") val episodeNumber: Any?,
    @SerializedName("title") val title: String?,
    @SerializedName("container_extension") val containerExtension: String?, // Já mapeado corretamente para Episódios
    @SerializedName("info") val info: XtreamEpisodeDetail?
)

data class XtreamEpisodeDetail(
    @SerializedName("movie_image") val movieImage: String?,
    @SerializedName("plot") val plot: String?
)

data class XtreamPanel(
    @SerializedName("user_info") val userInfo: XtreamUser?,
    @SerializedName("categories") val categories: Map<String, List<XtreamCategory>>?
)

data class XtreamUser(
    val status: String,
    val exp_date: String?,
    val active_cons: String?
)

data class XtreamVodResponse(
    @SerializedName("info") val info: XtreamVodInfo?,
    @SerializedName("movie_data") val movieData: XtreamVodData?
)

data class XtreamVodInfo(
    @SerializedName("name") val name: String?,
    @SerializedName("movie_image") val movieImage: String?,
    @SerializedName("plot") val plot: String?,
    @SerializedName("rating") val rating: String?,
    @SerializedName("releasedate") val releaseDate: String?,
    @SerializedName("genre") val genre: String?,
    @SerializedName("director") val director: String?,
    @SerializedName("actors") val actors: String?,
    @SerializedName("duration") val duration: String?,
    @SerializedName("tmdb_id") val tmdbId: String?
)

data class XtreamVodData(
    @SerializedName("stream_id") val streamId: Int?,
    @SerializedName("name") val name: String?,
    @SerializedName("container_extension") val containerExtension: String?
)