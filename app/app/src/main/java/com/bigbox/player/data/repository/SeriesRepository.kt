package com.bigbox.player.data.repository

import com.bigbox.player.data.local.SeriesDao
import com.bigbox.player.data.local.SeriesEntity
import com.bigbox.player.data.local.SeriesCategoryEntity
import com.bigbox.player.data.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class SeriesRepository(
    private val host: String,
    private val seriesDao: SeriesDao,
    private val externalScope: CoroutineScope
) {
    private val service = RetrofitClient.getXtreamService(host)

    /**
     * Retorna um Flow direto do Banco de Dados.
     * Dispara a atualização da API em background.
     */
    fun getSeriesCategories(user: String, pass: String): Flow<List<SeriesCategoryEntity>> {
        // Disparar atualização em background
        externalScope.launch(Dispatchers.IO) {
            try {
                val response = service.getCategories(user, pass, "get_series_categories")
                if (response.isSuccessful && response.body() != null) {
                    val entities = response.body()!!.map { 
                        SeriesCategoryEntity(it.id, it.name) 
                    }
                    seriesDao.insertCategories(entities)
                }
            } catch (e: Exception) {
                // Falha silenciosa: o usuário continuará vendo o cache
            }
        }
        return seriesDao.getCategories()
    }

    /**
     * Retorna Séries por categoria via cache.
     * Atualiza o cache via API de forma assíncrona.
     */
    fun getSeriesByCategory(user: String, pass: String, catId: String): Flow<List<SeriesEntity>> {
        externalScope.launch(Dispatchers.IO) {
            try {
                val response = service.getStreams(user, pass, "get_series", catId)
                if (response.isSuccessful && response.body() != null) {
                    val entities = response.body()!!.map { stream ->
                        SeriesEntity(
                            seriesId = if (stream.seriesId != null && stream.seriesId != 0) stream.seriesId else stream.streamId,
                            name = stream.name,
                            cover = stream.cover ?: stream.icon,
                            categoryId = catId
                        )
                    }
                    seriesDao.insertSeries(entities)
                }
            } catch (e: Exception) {
                // Log error
            }
        }
        return seriesDao.getSeriesByCategory(catId)
    }
}