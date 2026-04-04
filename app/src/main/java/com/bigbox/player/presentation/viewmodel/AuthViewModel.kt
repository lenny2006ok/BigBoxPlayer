package com.bigbox.player.presentation.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bigbox.player.data.remote.AuthRequest
import com.bigbox.player.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.util.Locale

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val playlistUrl: String, val mac: String) : AuthUiState()
    data class Error(val message: String, val mac: String) : AuthUiState()
}

class AuthViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _activationUrl = MutableStateFlow("")
    val activationUrl: StateFlow<String> = _activationUrl.asStateFlow()

    fun startAuthentication(context: Context) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            
            val mac = generatePseudoMac(context)
            val splashJob = launch { delay(8000) }
            
            try {
                val response = RetrofitClient.apiService.authenticate(AuthRequest(mac))
                splashJob.join()

                if (response.isSuccessful && response.body()?.status == "success") {
                    val url = response.body()?.data?.playlistUrl
                    if (url != null) {
                        _uiState.value = AuthUiState.Success(url, mac)
                    } else {
                        _uiState.value = AuthUiState.Error("Playlist não configurada.", mac)
                    }
                } else {
                    _uiState.value = AuthUiState.Error(response.body()?.message ?: "Dispositivo Bloqueado", mac)
                }
            } catch (e: Exception) {
                splashJob.join()
                _uiState.value = AuthUiState.Error("Erro de Conexão: ${e.message}", mac)
            }
        }
    }

    fun fetchActivationCredentials(deviceMac: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getDeviceCode(deviceMac)
                val devicePass = if (response.isSuccessful && !response.body()?.password.isNullOrEmpty()) {
                    response.body()!!.password!!
                } else {
                    "admin"
                }
                _activationUrl.value = "https://bigbox.eletrixtv.top/playlist.php?mac=$deviceMac&pass=$devicePass"
            } catch (e: Exception) {
                _activationUrl.value = "https://bigbox.eletrixtv.top/playlist.php?mac=$deviceMac&pass=admin"
            }
        }
    }

    @SuppressLint("HardwareIds")
    private fun generatePseudoMac(context: Context): String {
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "000000000000"
        val sb = StringBuilder()
        for (i in 0 until androidId.length.coerceAtMost(12) step 2) {
            sb.append(androidId.substring(i, i + 2).uppercase(Locale.ROOT))
            if (i < 10 && i + 2 < androidId.length) sb.append(":")
        }
        while (sb.length < 17) {
            if (sb.isNotEmpty() && sb.last() != ':') sb.append(":")
            sb.append("00")
        }
        return sb.toString().take(17)
    }
}