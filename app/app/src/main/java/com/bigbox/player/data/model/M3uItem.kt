package com.bigbox.player.data.model

data class M3uItem(
    val title: String,
    val url: String,
    val logo: String? = null,
    val category: String? = null
)