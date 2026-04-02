package com.bigbox.player.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbService {
    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: String,
        @Query("language") language: String = "pt-BR"
    ): Response<TmdbMovieDetail>
}

data class TmdbMovieDetail(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String?,
    @SerializedName("overview") val overview: String?,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("backdrop_path") val backdropPath: String?,
    @SerializedName("release_date") val releaseDate: String?,
    @SerializedName("vote_average") val voteAverage: Double?,
    @SerializedName("runtime") val runtime: Int?,
    @SerializedName("genres") val genres: List<TmdbGenre>?
)

data class TmdbGenre(
    @SerializedName("name") val name: String
)
