package com.bigbox.player.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SeriesDao {
    @Query("SELECT * FROM series_categories")
    fun getCategories(): Flow<List<SeriesCategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<SeriesCategoryEntity>)

    @Query("SELECT * FROM series WHERE categoryId = :catId")
    fun getSeriesByCategory(catId: String): Flow<List<SeriesEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeries(series: List<SeriesEntity>)

    @Query("DELETE FROM series WHERE categoryId = :catId")
    suspend fun clearSeriesByCategory(catId: String)
}