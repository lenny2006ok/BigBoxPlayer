package com.bigbox.player.data.parser

import com.bigbox.player.data.local.ChannelEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

class OptimizedM3uParser {

    suspend fun parseInMemory(inputStream: InputStream): List<ChannelEntity> = withContext(Dispatchers.IO) {
        val reader = BufferedReader(InputStreamReader(inputStream))
        val channels = mutableListOf<ChannelEntity>()
        var currentCategory = "tv"
        var currentName = ""
        var currentLogo: String? = null
        var line: String?

        while (reader.readLine().also { line = it } != null) {
            val trimmedLine = line!!.trim()
            
            if (trimmedLine.startsWith("#EXTINF")) {
                currentLogo = extractAttribute(trimmedLine, "tvg-logo")
                currentCategory = extractAttribute(trimmedLine, "group-title") ?: "Uncategorized"
                currentName = trimmedLine.substringAfterLast(",").trim()
            } else if (trimmedLine.startsWith("http")) {
                channels.add(
                    ChannelEntity(
                        name = currentName,
                        streamUrl = trimmedLine,
                        logoUrl = currentLogo,
                        category = currentCategory
                    )
                )
            }
        }
        reader.close()
        return@withContext channels
    }

    private fun extractAttribute(line: String, attribute: String): String? {
        val regex = Regex("$attribute=\"([^\"]+)\"")
        return regex.find(line)?.groupValues?.get(1)
    }
}