package com.bigbox.player.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

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

data class CodeResponse(
    val mac: String,
    val password: String?
)

interface ApiService {
    @POST("api.php")
    suspend fun authenticate(@Body request: AuthRequest): Response<AuthResponse>

    @GET("code.php")
    suspend fun getDeviceCode(@Query("mac") mac: String): Response<CodeResponse>
}