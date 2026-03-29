package com.bigbox.player.data.repository

import android.util.Log
import com.bigbox.player.data.remote.RetrofitClient
import com.bigbox.player.data.remote.XtreamCategory
import com.bigbox.player.data.remote.XtreamSeriesInfo
import com.bigbox.player.data.remote.XtreamStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class XtreamRepository(
    private val host: String,
    private val user: String? = null,
    private val pass: String? = null
) {

    private val service = RetrofitClient.getXtreamService(host)
    private val TAG = "DEBUG_PLAYER"

    suspend fun getCategories(u: String, p: String, action: String): Result<List<XtreamCategory>> =
        withContext(Dispatchers.IO) {
            try {
                val response = service.getCategories(u, p, action)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("Erro na API"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getStreams(u: String, p: String, action: String, catId: String): Result<List<XtreamStream>> =
        withContext(Dispatchers.IO) {
            try {
                val response = service.getStreams(u, p, action, catId)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("Erro na API"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getSeriesInfo(u: String, p: String, seriesId: Int): Result<XtreamSeriesInfo> =
        withContext(Dispatchers.IO) {
            try {
                val response = service.getSeriesInfo(user ?: u, pass ?: p, seriesId = seriesId)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("Erro ao buscar detalhes"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * RECONSTRUÇÃO DO URL BUILDER (PASSO 2)
     * Regra obrigatória Xtream Codes para rotas dinâmicas.
     */
    fun buildStreamUrl(streamId: String, type: String, extension: String?): String {
        val cleanHost = host.removeSuffix("/")
        val baseUrl = if (cleanHost.startsWith("http")) cleanHost else "http://$cleanHost"
        
        val (path, fileExt) = when (type.lowercase()) {
            "movie" -> "movie" to ".${extension ?: "mp4"}"
            "series" -> "series" to ".${extension ?: "mp4"}"
            "live" -> "live" to "" // Canais não usam extensão ou usam .ts automático
            else -> "live" to ""
        }

        val finalUrl = "$baseUrl/$path/$user/$pass/$streamId$fileExt"
        
        Log.e(TAG, "--------------------------------------------------")
        Log.e(TAG, "TYPE DETECTADO: $type")
        Log.e(TAG, "URL FINAL GERADA: $finalUrl")
        Log.e(TAG, "--------------------------------------------------")
        
        return finalUrl
    }
}