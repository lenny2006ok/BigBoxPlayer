package com.bigbox.player.presentation.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.Coil
import coil.ImageLoader
import com.bigbox.player.data.remote.RetrofitClient
import com.bigbox.player.databinding.ActivitySplashBinding
import com.bigbox.player.presentation.viewmodel.AuthUiState
import com.bigbox.player.presentation.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val imageLoader = ImageLoader.Builder(this)
            .okHttpClient(RetrofitClient.okHttpClient)
            .build()
        Coil.setImageLoader(imageLoader)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()
        viewModel.startAuthentication(this)
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is AuthUiState.Loading -> {
                            binding.loadingBar.visibility = View.VISIBLE
                            binding.txtStatus.text = "Autenticando..."
                        }
                        is AuthUiState.Success -> {
                            navigateToMain(state.playlistUrl, state.mac)
                        }
                        is AuthUiState.Error -> {
                            showError(state.message)
                        }
                        else -> Unit
                    }
                }
            }
        }
    }

    private fun navigateToMain(url: String, mac: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("PLAYLIST_URL", url)
            putExtra("MAC_ADDRESS", mac)
        }
        startActivity(intent)
        finish()
    }

    private fun showError(msg: String) {
        binding.loadingBar.visibility = View.GONE
        binding.txtStatus.text = msg
        binding.txtStatus.setTextColor(android.graphics.Color.RED)
    }
}