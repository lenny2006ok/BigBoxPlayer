package com.bigbox.player.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class AuthRequest(
    @SerializedName("mac_address") val macAddress: String
)

data class AuthResponse(
    val status: String,
    val message: String?,
    val data: AuthData?
)

data class AuthData(
    @SerializedName("playlist_url") val playlistUrl: String?,
    val theme: ThemeConfig?
)

data class ThemeConfig(
    @SerializedName("logo_url") val logoUrl: String?,
    @SerializedName("tv_name") val tvName: String?,
    @SerializedName("movies_name") val moviesName: String?,
    @SerializedName("series_name") val seriesName: String?
)

interface ApiService {
    // No seu PHP o arquivo se chama api.php
    @POST("api.php")
    suspend fun authenticate(@Body request: AuthRequest): Response<AuthResponse>
}