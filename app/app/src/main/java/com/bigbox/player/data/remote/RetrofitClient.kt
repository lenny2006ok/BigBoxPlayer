package com.bigbox.player.data.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object RetrofitClient {
    private const val BASE_URL = "https://bigbox.eletrixtv.top/"
    private const val TMDB_BASE_URL = "https://api.themoviedb.org/3/"
    private const val TMDB_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJmNDYxYjY1NjhlOTI0Mjg5NzBhZWE5MmY4NDM4ZDQwMiIsIm5iZiI6MTc0Mzg2Mjk5My45MDQsInN1YiI6IjY3ZjEzY2QxMmY3ZDQzNzAyNzk5OTRmMSIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.564cgJsVj4pB1DADb6OwvXhVIjyxBqmpUl5AoD-JVT0"

    val okHttpClient: OkHttpClient by lazy {
        try {
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            })

            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())

            OkHttpClient.Builder()
                .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
                .hostnameVerifier { _, _ -> true }
                .build()
        } catch (e: Exception) {
            OkHttpClient.Builder().build()
        }
    }

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    fun getXtreamService(hostUrl: String): XtreamService {
        // Limpeza de URL para evitar crash de porta -1
        val cleanUrl = if (hostUrl.endsWith("/")) hostUrl else "$hostUrl/"
        return Retrofit.Builder()
            .baseUrl(cleanUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(XtreamService::class.java)
    }

    val tmdbService: TmdbService by lazy {
        val tmdbClient = okHttpClient.newBuilder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $TMDB_TOKEN")
                    .addHeader("accept", "application/json")
                    .build()
                chain.proceed(request)
            }
            .build()

        Retrofit.Builder()
            .baseUrl(TMDB_BASE_URL)
            .client(tmdbClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TmdbService::class.java)
    }
}