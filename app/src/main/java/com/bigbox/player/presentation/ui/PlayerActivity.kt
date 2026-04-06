package com.bigbox.player.presentation.ui

import android.content.Context
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bigbox.player.data.repository.XtreamRepository
import com.bigbox.player.data.utils.PlayerSettingsManager
import com.bigbox.player.databinding.ActivityPlayerBinding
import com.bigbox.player.presentation.player.PlayerFactory
import com.bigbox.player.presentation.player.VideoEngine
import com.bigbox.player.presentation.viewmodel.PlayerViewModel
import com.bigbox.player.presentation.viewmodel.SeriesViewModelFactory
import kotlinx.coroutines.launch
import java.util.ArrayList

class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private var videoEngine: VideoEngine? = null

    // Variável para controlar o estado do Play/Pause
    private var isVideoPlaying = true

    // Variáveis para a troca de canal (Zapping)
    private var streamIds: ArrayList<String>? = null
    private var currentIndex: Int = -1

    private val viewModel: PlayerViewModel by viewModels {
        val playlistUrl = intent.getStringExtra("PLAYLIST_URL") ?: ""
        val uri = Uri.parse(playlistUrl)
        val host = "${uri.scheme}://${uri.host}${if (uri.port != -1) ":${uri.port}" else ""}"
        val user = uri.getQueryParameter("username") ?: ""
        val pass = uri.getQueryParameter("password") ?: ""
        SeriesViewModelFactory(XtreamRepository(host, user, pass))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Captura a lista de canais e o índice atual (se enviados pela tela anterior)
        streamIds = intent.getStringArrayListExtra("STREAM_IDS")
        currentIndex = intent.getIntExtra("CURRENT_INDEX", -1)

        val streamId = intent.getStringExtra("STREAM_ID") ?: ""
        val type = intent.getStringExtra("STREAM_TYPE") ?: "live"
        val extension = intent.getStringExtra("CONTAINER_EXTENSION")

        setupObservers(type)
        viewModel.prepareStream(streamId, type, extension)
    }

    private fun setupObservers(contentType: String) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.streamUrl.collect { url ->
                    if (url.isNotEmpty()) {
                        binding.txtPlayerStatus.visibility = View.GONE
                        Log.d("DEBUG_PLAYER", "Iniciando Motor de Vídeo com URL: $url")
                        initializeEngine(url, contentType)
                    } else {
                        binding.txtPlayerStatus.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun initializeEngine(url: String, contentType: String) {
        videoEngine?.release()

        val settingsManager = PlayerSettingsManager(this)
        val playerFactory = PlayerFactory(settingsManager)

        videoEngine = playerFactory.createEngine(this, contentType)

        videoEngine?.let { engine ->
            engine.init(this, binding.playerContainer)
            engine.playUrl(url)
            isVideoPlaying = true // Sempre que inicia, está tocando
        }
    }

    override fun onResume() {
        super.onResume()
        videoEngine?.resume()
        isVideoPlaying = true
    }

    override fun onPause() {
        super.onPause()
        videoEngine?.pause()
        isVideoPlaying = false
    }

    override fun onStop() {
        super.onStop()
        videoEngine?.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        videoEngine?.release()
        videoEngine = null
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                adjustVolume(AudioManager.ADJUST_RAISE)
                return true
            }
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                adjustVolume(AudioManager.ADJUST_LOWER)
                return true
            }
            KeyEvent.KEYCODE_DPAD_UP -> {
                changeChannel(1) // Próximo
                return true
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                changeChannel(-1) // Anterior
                return true
            }
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                togglePlayPause()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun adjustVolume(direction: Int) {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.adjustStreamVolume(
            AudioManager.STREAM_MUSIC,
            direction,
            AudioManager.FLAG_SHOW_UI
        )
    }

    private fun togglePlayPause() {
        if (isVideoPlaying) {
            videoEngine?.pause()
            isVideoPlaying = false
            Toast.makeText(this, "Pausado", Toast.LENGTH_SHORT).show()
        } else {
            videoEngine?.resume()
            isVideoPlaying = true
            Toast.makeText(this, "Reproduzindo", Toast.LENGTH_SHORT).show()
        }
    }

    private fun changeChannel(direction: Int) {
        // Verifica se a lista de canais chegou corretamente
        if (streamIds != null && streamIds!!.isNotEmpty() && currentIndex != -1) {

            // Atualiza a posição na lista
            currentIndex += direction

            // Lógica de Loop: se passar do último canal, volta pro primeiro.
            if (currentIndex >= streamIds!!.size) currentIndex = 0
            if (currentIndex < 0) currentIndex = streamIds!!.size - 1

            val nextStreamId = streamIds!![currentIndex]
            val type = intent.getStringExtra("STREAM_TYPE") ?: "live"
            val extension = intent.getStringExtra("CONTAINER_EXTENSION")

            binding.txtPlayerStatus.visibility = View.VISIBLE
            binding.txtPlayerStatus.text = "Trocando canal..."

            // Recarrega o motor com o novo canal
            viewModel.prepareStream(nextStreamId, type, extension)

        } else {
            // Caso a lista não tenha sido enviada pela LiveActivity
            Toast.makeText(this, "Modo Zapping Indisponível", Toast.LENGTH_SHORT).show()
        }
    }
}