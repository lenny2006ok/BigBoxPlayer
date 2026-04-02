package com.bigbox.player.presentation.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import coil.Coil
import coil.ImageLoader
import coil.load
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
                            binding.loadingContainer.visibility = View.VISIBLE
                            binding.txtStatus.text = "Autenticando..."
                        }
                        is AuthUiState.Success -> {
                            showSuccessAnimation(state.playlistUrl, state.mac)
                        }
                        is AuthUiState.Error -> {
                            showErrorWithAnimation(state.message, state.mac)
                        }
                        else -> Unit
                    }
                }
            }
        }
    }

    private fun showErrorWithAnimation(msg: String, mac: String) {
        // Esconder o loading
        binding.loadingContainer.animate().alpha(0f).setDuration(500).withEndAction {
            binding.loadingContainer.visibility = View.GONE
        }.start()

        // Configurar os dados
        binding.txtErrorTitle.text = msg
        binding.txtMac.text = "MAC: $mac"
        
        // Gerar QR Code via API pública para facilitar
        val qrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=$mac"
        binding.qrCodeImage.load(qrUrl)

        // Iniciar a transição de layout
        val constraintLayout = binding.root
        val constraintSet = ConstraintSet()
        constraintSet.clone(constraintLayout)

        // Mover o logo para a esquerda (alinhar o lado direito do logo com a guia central)
        constraintSet.clear(binding.imgLogo.id, ConstraintSet.END)
        constraintSet.connect(binding.imgLogo.id, ConstraintSet.END, binding.guidelineCenter.id, ConstraintSet.START)
        constraintSet.setMargin(binding.imgLogo.id, ConstraintSet.END, 32)

        val transition = AutoTransition()
        transition.duration = 1000
        TransitionManager.beginDelayedTransition(constraintLayout, transition)
        constraintSet.applyTo(constraintLayout)

        // Fade in do gradiente e do container de MAC
        binding.leftGradient.animate().alpha(1f).setDuration(1000).start()
        binding.macQrContainer.animate().alpha(1f).setDuration(1200).start()
    }

    private fun showSuccessAnimation(playlistUrl: String, mac: String) {
        // Esconder o loading e qualquer resquício do gradiente de 50%
        binding.loadingContainer.animate().alpha(0f).setDuration(300).start()
        binding.leftGradient.animate().alpha(0f).setDuration(300).start()
        binding.macQrContainer.animate().alpha(0f).setDuration(300).start()

        // Ativar o fundo total (Gradiente preenchendo toda a tela)
        binding.fullGradient.animate().alpha(1f).setDuration(500).start()

        // Explosão do Logo (Escala 10x para sair da tela + Alpha 0)
        binding.imgLogo.animate()
            .scaleX(10f)
            .scaleY(10f)
            .alpha(0f)
            .setDuration(900)
            .withEndAction {
                navigateToMain(playlistUrl, mac)
                // Transição suave entre as telas
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
            .start()
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