package com.bigbox.player.presentation.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bigbox.player.R
import com.bigbox.player.data.utils.PlayerSettingsManager
import com.bigbox.player.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var settingsManager: PlayerSettingsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        settingsManager = PlayerSettingsManager(this)

        setupInitialStates()
        setupListeners()
    }

    private fun setupInitialStates() {
        // Canais
        if (settingsManager.getLivePlayer() == PlayerSettingsManager.TYPE_EXO) {
            binding.rbExoLive.isChecked = true
        } else {
            binding.rbVlcLive.isChecked = true
        }

        // Filmes
        if (settingsManager.getMoviePlayer() == PlayerSettingsManager.TYPE_EXO) {
            binding.rbExoMovies.isChecked = true
        } else {
            binding.rbVlcMovies.isChecked = true
        }

        // Séries
        if (settingsManager.getSeriesPlayer() == PlayerSettingsManager.TYPE_EXO) {
            binding.rbExoSeries.isChecked = true
        } else {
            binding.rbVlcSeries.isChecked = true
        }
    }

    private fun setupListeners() {
        binding.rgLive.setOnCheckedChangeListener { _, checkedId ->
            val type = if (checkedId == R.id.rbExoLive) PlayerSettingsManager.TYPE_EXO else PlayerSettingsManager.TYPE_VLC
            settingsManager.setLivePlayer(type)
        }

        binding.rgMovies.setOnCheckedChangeListener { _, checkedId ->
            val type = if (checkedId == R.id.rbExoMovies) PlayerSettingsManager.TYPE_EXO else PlayerSettingsManager.TYPE_VLC
            settingsManager.setMoviePlayer(type)
        }

        binding.rgSeries.setOnCheckedChangeListener { _, checkedId ->
            val type = if (checkedId == R.id.rbExoSeries) PlayerSettingsManager.TYPE_EXO else PlayerSettingsManager.TYPE_VLC
            settingsManager.setSeriesPlayer(type)
        }
    }
}
