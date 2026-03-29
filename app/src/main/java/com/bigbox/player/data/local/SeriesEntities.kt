package com.bigbox.player.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "series_categories")
data class SeriesCategoryEntity(
    @PrimaryKey val id: String,
    val name: String
)

@Entity(tableName = "series")
data class SeriesEntity(
    @PrimaryKey val seriesId: Int,
    val name: String,
    val cover: String?,
    val categoryId: String,
    val lastUpdated: Long = System.currentTimeMillis()
)