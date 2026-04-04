package com.bigbox.player.presentation.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.net.URLEncoder

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
                
                // 1. Escuta a URL de ativação para atualizar o QR Code
                launch {
                    viewModel.activationUrl.collect { urlCompleta ->
                        if (urlCompleta.isNotEmpty()) {
                            // CORREÇÃO CRÍTICA: Encode da URL completa para que o '&' não quebre o link do QR
                            val encodedData = URLEncoder.encode(urlCompleta, "UTF-8")
                            val qrGeneratorUrl = "https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=$encodedData"
                            
                            binding.qrCodeImage.load(qrGeneratorUrl)
                            Log.d("QR_DEBUG", "URL Enviada para QR: $urlCompleta")
                        }
                    }
                }

                // 2. Escuta o estado da autenticação
                launch {
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
                                viewModel.fetchActivationCredentials(state.mac)
                            }
                            else -> Unit
                        }
                    }
                }
            }
        }
    }

    private fun showErrorWithAnimation(msg: String, mac: String) {
        // Evita rodar a animação se já estivermos na tela de erro
        if (binding.macQrContainer.alpha > 0f) {
            binding.txtErrorTitle.text = msg
            binding.txtMac.text = "MAC: $mac"
            return
        }

        // Esconder o loading
        binding.loadingContainer.animate().alpha(0f).setDuration(500).withEndAction {
            binding.loadingContainer.visibility = View.GONE
        }.start()

        // Configurar os dados
        binding.txtErrorTitle.text = msg
        binding.txtMac.text = "MAC: $mac"

        // Iniciar a transição de layout
        val constraintLayout = binding.root
        val constraintSet = ConstraintSet()
        constraintSet.clone(constraintLayout)

        // Mover o logo para a esquerda
        constraintSet.clear(binding.imgLogo.id, ConstraintSet.END)
        constraintSet.connect(binding.imgLogo.id, ConstraintSet.END, binding.guidelineCenter.id, ConstraintSet.START)
        constraintSet.setMargin(binding.imgLogo.id, ConstraintSet.END, 32)

        val transition = AutoTransition()
        transition.duration = 1000
        TransitionManager.beginDelayedTransition(constraintLayout, transition)
        constraintSet.applyTo(constraintLayout)

        // Fade in do gradiente e do container de MAC/QR
        binding.leftGradient.animate().alpha(1f).setDuration(1000).start()
        binding.macQrContainer.animate().alpha(1f).setDuration(1200).start()
    }

    private fun showSuccessAnimation(playlistUrl: String, mac: String) {
        binding.loadingContainer.animate().alpha(0f).setDuration(300).start()
        binding.leftGradient.animate().alpha(0f).setDuration(300).start()
        binding.macQrContainer.animate().alpha(0f).setDuration(300).start()

        binding.fullGradient.animate().alpha(1f).setDuration(500).start()

        binding.imgLogo.animate()
            .scaleX(10f)
            .scaleY(10f)
            .alpha(0f)
            .setDuration(900)
            .withEndAction {
                navigateToMain(playlistUrl, mac)
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
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}
