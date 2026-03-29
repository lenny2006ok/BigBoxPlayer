package com.bigbox.player.data.parser

import com.bigbox.player.data.model.M3uItem
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

object M3uParser {
    fun parse(inputStream: InputStream): List<M3uItem> {
        val items = mutableListOf<M3uItem>()
        val reader = BufferedReader(InputStreamReader(inputStream))
        var line: String?
        var currentTitle = ""
        var currentLogo = ""
        var currentCategory = ""

        try {
            while (reader.readLine().also { line = it } != null) {
                if (line?.startsWith("#EXTINF") == true) {
                    // Pegar título
                    currentTitle = line?.substringAfterLast(",") ?: "Canal"
                    
                    // Pegar logo
                    currentLogo = line?.substringAfter("tvg-logo=\"")?.substringBefore("\"") ?: ""
                    
                    // Pegar categoria
                    currentCategory = line?.substringAfter("group-title=\"")?.substringBefore("\"") ?: "Outros"
                } else if (line?.startsWith("http") == true) {
                    items.add(
                        M3uItem(
                            title = currentTitle,
                            url = line ?: "",
                            logo = currentLogo,
                            category = currentCategory
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            reader.close()
        }
        return items
    }
}