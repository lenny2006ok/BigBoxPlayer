package com.bigbox.player.data.local

// Comentado temporariamente para resolver erro de build com Gradle 9.0
/*
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ChannelDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(channels: List<ChannelEntity>)

    @Query("SELECT * FROM channels WHERE category = :category ORDER BY name ASC")
    fun getChannelsByCategory(category: String): PagingSource<Int, ChannelEntity>

    @Query("DELETE FROM channels")
    suspend fun clearAll()
}
*/