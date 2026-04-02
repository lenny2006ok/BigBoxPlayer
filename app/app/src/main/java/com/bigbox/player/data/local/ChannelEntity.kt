package com.bigbox.player.data.local

// Comentado temporariamente para resolver erro de build sem a biblioteca Room
/*
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "channels",
    indices = [Index(value = ["category"])]
)
*/
data class ChannelEntity(
    /* @PrimaryKey(autoGenerate = true) */ val id: Int = 0,
    val name: String,
    val streamUrl: String,
    val logoUrl: String?,
    val category: String,
    val isFavorite: Boolean = false
)