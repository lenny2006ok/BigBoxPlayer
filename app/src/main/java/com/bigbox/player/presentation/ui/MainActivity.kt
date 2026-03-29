package com.bigbox.player.presentation.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.bigbox.player.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val webviewBaseUrl = "https://bigbox.eletrixtv.top/webviews/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val macAddress = intent.getStringExtra("MAC_ADDRESS") ?: "00:00:00:00:00:00"
        
        setupUI(macAddress)
        loadCategoryImages()
    }

    private fun setupUI(mac: String) {
        val playlistUrl = intent.getStringExtra("PLAYLIST_URL") ?: ""

        // Atualiza o botão para mostrar o MAC com tamanho menor e formato correto
        binding.btnSupport.text = "mac:$mac"
        
        binding.btnSupport.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://bigbox.eletrixtv.top"))
            startActivity(browserIntent)
        }

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

    private fun loadCategoryImages() {
        binding.imgCanais.load(webviewBaseUrl + "canais.png")
        binding.imgFilmes.load(webviewBaseUrl + "filmes.png")
        binding.imgSeries.load(webviewBaseUrl + "series.png")
    }
}