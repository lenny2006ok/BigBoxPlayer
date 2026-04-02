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
import androidx.recyclerview.widget.LinearLayoutManager
import com.bigbox.player.data.repository.XtreamRepository
import com.bigbox.player.databinding.ActivityCategoryBinding
import com.bigbox.player.presentation.viewmodel.SeriesUiState
import com.bigbox.player.presentation.viewmodel.SeriesViewModel
import com.bigbox.player.presentation.viewmodel.SeriesViewModelFactory
import kotlinx.coroutines.launch

class SeriesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryBinding
    private var user: String = ""
    private var pass: String = ""

    private val viewModel: SeriesViewModel by viewModels {
        val playlistUrl = intent.getStringExtra("PLAYLIST_URL") ?: ""
        val uri = Uri.parse(playlistUrl)
        val extractedHost = "${uri.scheme}://${uri.host}${if (uri.port != -1) ":${uri.port}" else ""}"
        SeriesViewModelFactory(XtreamRepository(extractedHost))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupObservers()
        initData()
    }

    private fun initData() {
        val playlistUrl = intent.getStringExtra("PLAYLIST_URL") ?: ""
        if (playlistUrl.isNotEmpty()) {
            val uri = Uri.parse(playlistUrl)
            user = uri.getQueryParameter("username") ?: ""
            pass = uri.getQueryParameter("password") ?: ""
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
                        is SeriesUiState.Loading -> {
                            binding.loadingCategory.visibility = View.VISIBLE
                        }
                        is SeriesUiState.SuccessCategories -> {
                            binding.loadingCategory.visibility = View.GONE
                            
                            // Configura Sidebar
                            binding.recyclerSidebar.layoutManager = LinearLayoutManager(this@SeriesActivity)
                            binding.recyclerSidebar.adapter = CategoryAdapter(state.categories) { category ->
                                viewModel.loadStreams(category.id)
                            }

                            // Carrega a primeira categoria automaticamente
                            if (state.categories.isNotEmpty()) {
                                viewModel.loadStreams(state.categories[0].id)
                            }
                        }
                        is SeriesUiState.SuccessStreams -> {
                            binding.loadingCategory.visibility = View.GONE
                            binding.recyclerItems.layoutManager = GridLayoutManager(this@SeriesActivity, 5)
                            binding.recyclerItems.adapter = StreamAdapter(state.streams) { stream ->
                                // CORREÇÃO SÊNIOR: Capturar o ID correto da série
                                val correctId = if (stream.seriesId != null && stream.seriesId != 0) stream.seriesId else stream.streamId
                                
                                val playlistUrl = intent.getStringExtra("PLAYLIST_URL") ?: ""
                                val intent = Intent(this@SeriesActivity, SeriesDetailsActivity::class.java).apply {
                                    putExtra("SERIES_ID", correctId)
                                    putExtra("SERIES_NAME", stream.name)
                                    putExtra("SERIES_POSTER", stream.cover ?: stream.icon)
                                    putExtra("PLAYLIST_URL", playlistUrl)
                                }
                                startActivity(intent)
                            }
                        }
                        is SeriesUiState.Error -> {
                            binding.loadingCategory.visibility = View.GONE
                            Toast.makeText(this@SeriesActivity, state.message, Toast.LENGTH_SHORT).show()
                        }
                        else -> Unit
                    }
                }
            }
        }
    }
}