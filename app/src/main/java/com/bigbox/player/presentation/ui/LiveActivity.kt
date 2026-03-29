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
import com.bigbox.player.presentation.viewmodel.LiveUiState
import com.bigbox.player.presentation.viewmodel.LiveViewModel
import com.bigbox.player.presentation.viewmodel.LiveViewModelFactory
import kotlinx.coroutines.launch

class LiveActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryBinding

    private val viewModel: LiveViewModel by viewModels {
        val playlistUrl = intent.getStringExtra("PLAYLIST_URL") ?: ""
        val uri = Uri.parse(playlistUrl)
        val extractedHost = "${uri.scheme}://${uri.host}${if (uri.port != -1) ":${uri.port}" else ""}"
        LiveViewModelFactory(XtreamRepository(extractedHost))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.txtCategoryTitle.text = "CANAIS AO VIVO"
        
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
                        is LiveUiState.Loading -> binding.loadingCategory.visibility = View.VISIBLE
                        is LiveUiState.SuccessCategories -> {
                            binding.loadingCategory.visibility = View.GONE
                            binding.recyclerItems.layoutManager = GridLayoutManager(this@LiveActivity, 2)
                            binding.recyclerItems.adapter = CategoryAdapter(state.categories) { category ->
                                viewModel.loadStreams(category.id)
                            }
                        }
                        is LiveUiState.SuccessStreams -> {
                            binding.loadingCategory.visibility = View.GONE
                            binding.recyclerItems.layoutManager = GridLayoutManager(this@LiveActivity, 5)
                            binding.recyclerItems.adapter = StreamAdapter(state.streams) { stream ->
                                // SINCRONIZAÇÃO: Passar dados brutos para o Player
                                val playlistUrl = intent.getStringExtra("PLAYLIST_URL") ?: ""
                                startActivity(Intent(this@LiveActivity, PlayerActivity::class.java).apply {
                                    putExtra("PLAYLIST_URL", playlistUrl)
                                    putExtra("STREAM_ID", stream.streamId.toString())
                                    putExtra("STREAM_TYPE", "live")
                                    putExtra("STREAM_NAME", stream.name)
                                })
                            }
                        }
                        is LiveUiState.Error -> {
                            binding.loadingCategory.visibility = View.GONE
                            Toast.makeText(this@LiveActivity, state.message, Toast.LENGTH_SHORT).show()
                        }
                        else -> Unit
                    }
                }
            }
        }
    }
}