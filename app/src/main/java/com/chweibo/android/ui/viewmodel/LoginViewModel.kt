package com.chweibo.android.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chweibo.android.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>(
        extraBufferCapacity = 1,
        onBufferOverflow = kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
    )
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    val authUrl: String
        get() = authRepository.getAuthConfig().getAuthorizeUrl()

    fun isValidCallbackUrl(url: String): Boolean {
        val uri = android.net.Uri.parse(url)
        val redirectUri = android.net.Uri.parse(com.chweibo.android.BuildConfig.WEIBO_REDIRECT_URI)
        return uri.scheme == redirectUri.scheme &&
                uri.host == redirectUri.host &&
                uri.path == redirectUri.path
    }

    fun handleAuthCallback(url: String) {
        if (!isValidCallbackUrl(url)) {
            _uiState.update { it.copy(isLoading = false, error = "Invalid callback URL") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            authRepository.handleAuthCallback(url)
                .onSuccess { token ->
                    Log.d("LoginViewModel", "Token obtained: ${token.accessToken.take(10)}...")
                    _uiEvent.emit(UiEvent.Navigate("main"))
                }
                .onFailure { e ->
                    Log.e("LoginViewModel", "Auth failed", e)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "授权失败"
                        )
                    }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    data class LoginUiState(
        val isLoading: Boolean = false,
        val error: String? = null
    )
}
