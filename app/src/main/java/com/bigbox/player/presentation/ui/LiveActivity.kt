package com.bigbox.player.presentation.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bigbox.player.data.repository.XtreamRepository
import com.bigbox.player.databinding.ActivityCategoryBinding
import com.bigbox.player.presentation.viewmodel.LiveUiState
import com.bigbox.player.presentation.viewmodel.LiveViewModel
import com.bigbox.player.presentation.viewmodel.LiveViewModelFactory
import kotlinx.coroutines.launch

class LiveActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryBinding

    private val viewModel: LiveViewModel by viewModels {
        val playlistUrl = intent.getStringExtra("PLAYLIST_URL") ?: ""
        // Extração Blindada do Host (Ignora erros de porta do Uri.parse)
        val extractedHost = extractHostSafely(playlistUrl)
        Log.d("DEBUG_ELETRIX", "Host Extraído: $extractedHost")
        LiveViewModelFactory(XtreamRepository(extractedHost))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()
        initData()
    }

    private fun extractHostSafely(url: String): String {
        return try {
            val uri = android.net.Uri.parse(url)
            val scheme = uri.scheme ?: "http"
            val host = uri.host ?: ""
            val port = if (uri.port != -1) ":${uri.port}" else ""
            if (host.isNotEmpty()) "$scheme://$host$port" else url.substringBefore("/get.php")
        } catch (e: Exception) {
            url.substringBefore("/get.php")
        }
    }

    private fun initData() {
        val playlistUrl = intent.getStringExtra("PLAYLIST_URL") ?: ""
        Log.d("DEBUG_ELETRIX", "URL Completa Recebida: $playlistUrl")

        if (playlistUrl.isNotEmpty()) {
            // Extração Blindada via Regex para User e Pass
            val userRegex = "username=([^&]+)".toRegex()
            val passRegex = "password=([^&]+)".toRegex()

            val userMatch = userRegex.find(playlistUrl)
            val passMatch = passRegex.find(playlistUrl)

            val user = userMatch?.groupValues?.get(1) ?: ""
            val pass = passMatch?.groupValues?.get(1) ?: ""

            Log.d("DEBUG_ELETRIX", "Credenciais - User: $user | Pass: $pass")

            if (user.isNotEmpty() && pass.isNotEmpty()) {
                viewModel.loadCategories(user, pass)
            } else {
                Toast.makeText(this, "Erro: Credenciais inválidas na URL.", Toast.LENGTH_LONG).show()
                finish()
            }
        } else {
            Toast.makeText(this, "Erro: URL da Playlist Vazia.", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is LiveUiState.Loading -> binding.loadingCategory.visibility = View.VISIBLE
                        is LiveUiState.SuccessCategories -> {
                            binding.loadingCategory.visibility = View.GONE

                            binding.recyclerSidebar.layoutManager = LinearLayoutManager(this@LiveActivity)
                            binding.recyclerSidebar.adapter = CategoryAdapter(state.categories) { category ->
                                viewModel.loadStreams(category.id)
                            }

                            if (state.categories.isNotEmpty()) {
                                viewModel.loadStreams(state.categories[0].id)
                            }
                        }
                        is LiveUiState.SuccessStreams -> {
                            binding.loadingCategory.visibility = View.GONE
                            binding.recyclerItems.layoutManager = GridLayoutManager(this@LiveActivity, 5)

                            val currentCategoryStreamIds = ArrayList(state.streams.map { it.streamId.toString() })

                            binding.recyclerItems.adapter = StreamAdapter(state.streams) { stream ->
                                val clickedIndex = state.streams.indexOf(stream)
                                val playlistUrl = intent.getStringExtra("PLAYLIST_URL") ?: ""
                                startActivity(Intent(this@LiveActivity, PlayerActivity::class.java).apply {
                                    putExtra("PLAYLIST_URL", playlistUrl)
                                    putExtra("STREAM_ID", stream.streamId.toString())
                                    putExtra("STREAM_TYPE", "live")
                                    putExtra("STREAM_NAME", stream.name)
                                    putStringArrayListExtra("STREAM_IDS", currentCategoryStreamIds)
                                    putExtra("CURRENT_INDEX", clickedIndex)
                                })
                            }
                        }
                        is LiveUiState.Error -> {
                            binding.loadingCategory.visibility = View.GONE
                            // Log de erro real e feedback visual para ajudar no debug
                            Log.e("DEBUG_ELETRIX", "Erro da API: ${state.message}")
                            Toast.makeText(this@LiveActivity, "Erro no servidor: ${state.message}", Toast.LENGTH_LONG).show()
                        }
                        else -> Unit
                    }
                }
            }
        }
    }
}