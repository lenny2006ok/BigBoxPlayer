package com.bigbox.player.presentation.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.bigbox.player.data.remote.RetrofitClient
import com.bigbox.player.data.remote.XtreamStream
import com.bigbox.player.databinding.ActivityCategoryBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CategoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryBinding
    private var host: String = ""
    private var user: String = ""
    private var pass: String = ""
    private var action: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val title = intent.getStringExtra("TITLE") ?: "CATEGORIA"
        action = intent.getStringExtra("ACTION") ?: ""
        val playlistUrl = intent.getStringExtra("PLAYLIST_URL") ?: ""

        binding.txtCategoryTitle.text = title

        if (playlistUrl.isNotEmpty()) {
            parsePlaylistUrl(playlistUrl)
            loadCategories()
        } else {
            finish()
        }
    }

    private fun parsePlaylistUrl(url: String) {
        val uri = Uri.parse(url)
        host = "${uri.scheme}://${uri.host}:${uri.port}"
        user = uri.getQueryParameter("username") ?: ""
        pass = uri.getQueryParameter("password") ?: ""
    }

    private fun loadCategories() {
        binding.loadingCategory.visibility = View.VISIBLE
        val xtreamService = RetrofitClient.getXtreamService(host)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = xtreamService.getCategories(user, pass, action)
                withContext(Dispatchers.Main) {
                    binding.loadingCategory.visibility = View.GONE
                    if (response.isSuccessful && response.body() != null) {
                        setupCategoryList(response.body()!!)
                    } else {
                        Toast.makeText(this@CategoryActivity, "Erro ao carregar categorias", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.loadingCategory.visibility = View.GONE
                    Toast.makeText(this@CategoryActivity, "Erro de conexão", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupCategoryList(categories: List<com.bigbox.player.data.remote.XtreamCategory>) {
        binding.recyclerItems.layoutManager = GridLayoutManager(this, 2) // Categorias em 2 colunas
        binding.recyclerItems.adapter = CategoryAdapter(categories) { category ->
            loadStreams(category.id)
        }
    }

    private fun loadStreams(categoryId: String) {
        binding.loadingCategory.visibility = View.VISIBLE
        val xtreamService = RetrofitClient.getXtreamService(host)
        
        // Determinar a ação correta para buscar os canais/filmes daquela categoria
        val streamAction = when (action) {
            "get_live_categories" -> "get_live_streams"
            "get_vod_categories" -> "get_vod_streams"
            "get_series_categories" -> "get_series"
            else -> "get_live_streams"
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = xtreamService.getStreams(user, pass, streamAction, categoryId)
                withContext(Dispatchers.Main) {
                    binding.loadingCategory.visibility = View.GONE
                    if (response.isSuccessful && response.body() != null) {
                        setupStreamList(response.body()!!)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.loadingCategory.visibility = View.GONE
                }
            }
        }
    }

    private fun setupStreamList(streams: List<XtreamStream>) {
        binding.recyclerItems.layoutManager = GridLayoutManager(this, 5) // Canais em 5 colunas
        binding.recyclerItems.adapter = StreamAdapter(streams) { stream ->
            playStream(stream)
        }
    }

    private fun playStream(stream: XtreamStream) {
        // Formato Xtream: http://host:port/live/user/pass/stream_id.ts (ou m3u8/mp4)
        val type = if (action == "get_live_categories") "live" else if (action == "get_vod_categories") "movie" else "series"
        val extension = if (type == "live") "ts" else "mp4" // Simplificado
        
        val streamUrl = "$host/$type/$user/$pass/${stream.streamId}.$extension"
        
        val intent = Intent(this, PlayerActivity::class.java).apply {
            putExtra("STREAM_URL", streamUrl)
            putExtra("STREAM_NAME", stream.name)
        }
        startActivity(intent)
    }
}