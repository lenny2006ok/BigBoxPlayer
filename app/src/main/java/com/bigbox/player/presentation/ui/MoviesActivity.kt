package com.bigbox.player.presentation.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.bigbox.player.data.repository.XtreamRepository
import com.bigbox.player.databinding.ActivityCategoryBinding
import com.bigbox.player.presentation.viewmodel.MoviesUiState
import com.bigbox.player.presentation.viewmodel.MoviesViewModel
import com.bigbox.player.presentation.viewmodel.MoviesViewModelFactory
import kotlinx.coroutines.launch

class MoviesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryBinding

    private val viewModel: MoviesViewModel by viewModels {
        val playlistUrl = intent.getStringExtra("PLAYLIST_URL") ?: ""
        val uri = Uri.parse(playlistUrl)
        val extractedHost = "${uri.scheme}://${uri.host}${if (uri.port != -1) ":${uri.port}" else ""}"
        MoviesViewModelFactory(XtreamRepository(extractedHost))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.txtCategoryTitle.text = "FILMES"
        setupObservers()
        initData()
    }

    private fun initData() {
        val playlistUrl = intent.getStringExtra("PLAYLIST_URL") ?: ""
        if (playlistUrl.isNotEmpty()) {
            val uri = Uri.parse(playlistUrl)
            val user = uri.getQueryParameter("username") ?: ""
            val pass = uri.getQueryParameter("password") ?: ""
            viewModel.loadCategories(user, pass)
        } else {
            finish()
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is MoviesUiState.Loading -> binding.loadingCategory.visibility = View.VISIBLE
                        is MoviesUiState.SuccessCategories -> {
                            binding.loadingCategory.visibility = View.GONE
                            binding.recyclerItems.layoutManager = GridLayoutManager(this@MoviesActivity, 2)
                            binding.recyclerItems.adapter = CategoryAdapter(state.categories) { category ->
                                viewModel.loadStreams(category.id)
                            }
                        }
                        is MoviesUiState.SuccessStreams -> {
                            binding.loadingCategory.visibility = View.GONE
                            binding.recyclerItems.layoutManager = GridLayoutManager(this@MoviesActivity, 5)
                            binding.recyclerItems.adapter = StreamAdapter(state.streams) { stream ->
                                // SINCRONIZAÇÃO DE ORIGEM (PASSO 1)
                                val playlistUrl = intent.getStringExtra("PLAYLIST_URL") ?: ""
                                startActivity(Intent(this@MoviesActivity, PlayerActivity::class.java).apply {
                                    putExtra("PLAYLIST_URL", playlistUrl)
                                    putExtra("STREAM_ID", stream.streamId.toString())
                                    putExtra("STREAM_TYPE", "movie") // Forçando rota de filmes
                                    putExtra("CONTAINER_EXTENSION", stream.containerExtension)
                                    putExtra("STREAM_NAME", stream.name)
                                })
                            }
                        }
                        is MoviesUiState.Error -> {
                            binding.loadingCategory.visibility = View.GONE
                            Toast.makeText(this@MoviesActivity, state.message, Toast.LENGTH_SHORT).show()
                        }
                        else -> Unit
                    }
                }
            }
        }
    }
}