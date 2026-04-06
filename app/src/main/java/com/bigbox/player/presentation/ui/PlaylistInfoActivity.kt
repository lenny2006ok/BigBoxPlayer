package com.bigbox.player.presentation.ui

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load
import com.bigbox.player.databinding.ActivityPlaylistInfoBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

class PlaylistInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlaylistInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaylistInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val macAddress = intent.getStringExtra("MAC_ADDRESS") ?: "00:00:00:00:00:00"

        // Mantemos a consulta ao code.php para pegar a senha real do banco
        fetchRealPasswordFromServer(macAddress)
    }

    private fun fetchRealPasswordFromServer(mac: String) {
        binding.txtMacAddress.text = "Sincronizando..."
        binding.imgQrCode.visibility = View.INVISIBLE

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // CONSULTA: Puxa a senha atualizada do code.php
                val response = URL("https://bigbox.eletrixtv.top/code.php?mac=$mac").readText()
                val json = JSONObject(response)
                val realPassword = json.getString("password")

                withContext(Dispatchers.Main) {
                    val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                    prefs.edit().putString("device_pass", realPassword).apply()

                    // UI: Atualiza com a senha que veio do banco
                    updateUI(mac, realPassword)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    val savedPass = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                        .getString("device_pass", "admin") ?: "admin"
                    updateUI(mac, savedPass)
                }
            }
        }
    }

    private fun updateUI(mac: String, pass: String) {
        binding.txtMacAddress.text = "MAC: $mac"
        binding.txtDevicePass.text = "Device Key: $pass"
        binding.imgQrCode.visibility = View.VISIBLE

        // LÓGICA FINAL: O QR Code aponta para o playlist.php
        val urlFinalPlaylist = "https://bigbox.eletrixtv.top/playlist.php?mac=$mac&pass=$pass"

        // Codifica para a API de QR Code
        val qrCodeApiUrl = "https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=${Uri.encode(urlFinalPlaylist)}"

        binding.imgQrCode.load(qrCodeApiUrl) {
            crossfade(true)
            placeholder(android.R.drawable.progress_indeterminate_horizontal)
        }
    }
}