package com.chweibo.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.chweibo.android.data.local.SecureTokenDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val tokenRepo: SecureTokenDataStore
) : ViewModel() {

    suspend fun isLoggedIn(): Boolean {
        val loggedIn = tokenRepo.isLoggedIn.first()
        if (!loggedIn) return false

        val expiresAt = tokenRepo.expiresAt.first()
        val isValid = expiresAt > System.currentTimeMillis()
        if (!isValid) {
            tokenRepo.clearToken()
        }
        return isValid
    }
}
