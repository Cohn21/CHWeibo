package com.chweibo.android.data.api

import com.chweibo.android.data.local.SecureTokenDataStore
import com.chweibo.android.data.model.AccessToken
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenAuthenticator @Inject constructor(
    private val tokenRepo: SecureTokenDataStore,
    private val apiService: WeiboApiService
) : Authenticator {

    private val mutex = Mutex()

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.responseCount() >= 2) {
            return null
        }

        val currentToken = tokenRepo.getAccessTokenSync() ?: return null

        return runBlocking {
            mutex.withLock {
                val latestToken = tokenRepo.getAccessTokenSync()
                if (latestToken != currentToken) {
                    // Another thread already refreshed the token, retry with latest
                    return@withLock response.request.newBuilder()
                        .url(
                            response.request.url.newBuilder()
                                .setQueryParameter("access_token", latestToken)
                                .build()
                        )
                        .build()
                }

                val newToken = refreshTokenSynchronously()
                newToken?.let {
                    tokenRepo.saveToken(it)
                    response.request.newBuilder()
                        .url(
                            response.request.url.newBuilder()
                                .setQueryParameter("access_token", it.accessToken)
                                .build()
                        )
                        .build()
                } ?: run {
                    tokenRepo.clearToken()
                    null
                }
            }
        }
    }

    private suspend fun refreshTokenSynchronously(): AccessToken? {
        val refreshToken = tokenRepo.getRefreshTokenSync() ?: return null
        return try {
            apiService.refreshToken(refreshToken)
        } catch (e: Exception) {
            null
        }
    }

    private fun Response.responseCount(): Int {
        var count = 1
        var prior = this.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
