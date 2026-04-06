package com.bigbox.player.presentation.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bigbox.player.databinding.ActivitySettingsMenuBinding

class SettingsMenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Recebe os dados enviados pela MainActivity
        val macAddress = intent.getStringExtra("MAC_ADDRESS") ?: "00:00:00:00:00:00"
        val devicePass = intent.getStringExtra("DEVICE_PASS") ?: "admin"
        val playlistUrl = intent.getStringExtra("PLAYLIST_URL") ?: ""

        binding.btnMenuPlayer.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        binding.btnMenuPlaylist.setOnClickListener {
            val intent = Intent(this, PlaylistInfoActivity::class.java)

            // 2. Repassa TODOS os dados necessários para a tela do QR Code
            intent.putExtra("MAC_ADDRESS", macAddress)
            intent.putExtra("DEVICE_PASS", devicePass) // Essencial para o code.php
            intent.putExtra("PLAYLIST_URL", playlistUrl)

            startActivity(intent)
        }

        // Foco inicial para controle remoto de TV Box
        binding.btnMenuPlayer.requestFocus()
    }
}