package com.chweibo.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.chweibo.android.data.model.User
import com.chweibo.android.data.repository.AuthRepository
import com.chweibo.android.data.repository.WeiboRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val weiboRepository: WeiboRepository
) : ViewModel() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    val isLoggedIn: StateFlow<Boolean> = authRepository.isLoggedIn
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val homeTimeline = weiboRepository.getHomeTimeline()
        .cachedIn(viewModelScope)

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            if (isLoggedIn.value) {
                authRepository.getCurrentUser()
                    .onSuccess { user ->
                        _currentUser.value = user
                    }
                    .onFailure {
                        // 处理错误
                    }
            }
        }
    }

    fun refreshUserInfo() {
        viewModelScope.launch {
            authRepository.getCurrentUser()
                .onSuccess { user ->
                    _currentUser.value = user
                }
        }
    }

    fun logout(onLogoutComplete: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            _currentUser.value = null
            onLogoutComplete()
        }
    }
}
