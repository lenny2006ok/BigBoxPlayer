package com.bigbox.player.presentation.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bigbox.player.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val webviewBaseUrl = "https://bigbox.eletrixtv.top/webviews/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Recebe o MAC que veio da Splash
        val macAddress = intent.getStringExtra("MAC_ADDRESS") ?: "00:00:00:00:00:00"

        setupUI(macAddress)
    }

    private fun setupUI(mac: String) {
        // Capturamos a URL da playlist para os cards de conteúdo (Canais, Filmes, Séries)
        val playlistUrl = intent.getStringExtra("PLAYLIST_URL") ?: ""

        binding.btnSupport.text = "mac:$mac"

        binding.btnSupport.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://bigbox.eletrixtv.top"))
            startActivity(browserIntent)
        }

        // --- CÓDIGO DA ENGRENAGEM (Envia MAC, Senha de Ativação e URL da Playlist) ---
        binding.btnSettings.setOnClickListener {
            val intent = Intent(this, SettingsMenuActivity::class.java)

            // Buscamos a senha de ativação (Device Key) salva pela SplashActivity
            // Isso garante que o QR Code use a senha de pareamento real do painel
            val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            val devicePass = prefs.getString("device_pass", "admin") ?: "admin"

            intent.putExtra("MAC_ADDRESS", mac)
            intent.putExtra("DEVICE_PASS", devicePass) // Senha para o QR Code (code.php)
            intent.putExtra("PLAYLIST_URL", playlistUrl) // URL para carregar os conteúdos
            startActivity(intent)
        }
        // -------------------------------------------------------------------------

        binding.cardCanais.setOnClickListener {
            if (playlistUrl.isNotEmpty()) {
                val intent = Intent(this, LiveActivity::class.java)
                intent.putExtra("PLAYLIST_URL", playlistUrl)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Playlist não carregada", Toast.LENGTH_SHORT).show()
            }
        }

        binding.cardFilmes.setOnClickListener {
            if (playlistUrl.isNotEmpty()) {
                val intent = Intent(this, MoviesActivity::class.java)
                intent.putExtra("PLAYLIST_URL", playlistUrl)
                startActivity(intent)
            }
        }

        binding.cardSeries.setOnClickListener {
            if (playlistUrl.isNotEmpty()) {
                val intent = Intent(this, SeriesActivity::class.java)
                intent.putExtra("PLAYLIST_URL", playlistUrl)
                startActivity(intent)
            }
        }

        binding.cardCanais.requestFocus()
    }
}