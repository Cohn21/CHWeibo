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

    private val _events = MutableSharedFlow<LoginEvent>()
    val events: SharedFlow<LoginEvent> = _events.asSharedFlow()

    val authUrl: String
        get() = "https://api.weibo.com/oauth2/authorize?" +
                "client_id=${com.chweibo.android.BuildConfig.WEIBO_APP_KEY}&" +
                "redirect_uri=${com.chweibo.android.BuildConfig.WEIBO_REDIRECT_URI}&" +
                "scope=email,direct_messages_read,direct_messages_write," +
                "friendships_groups_read,friendships_groups_write," +
                "statuses_to_me_read,follow_app_official_microblog," +
                "invitation_write&display=mobile"

    fun handleAuthCallback(url: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            authRepository.handleAuthCallback(url)
                .onSuccess { token ->
                    Log.d("LoginViewModel", "Token obtained: ${token.accessToken.take(10)}...")
                    _events.emit(LoginEvent.NavigateToMain)
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

    sealed class LoginEvent {
        data object NavigateToMain : LoginEvent()
    }
}
