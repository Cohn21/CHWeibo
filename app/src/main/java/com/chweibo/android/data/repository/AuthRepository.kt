package com.chweibo.android.data.repository

import com.chweibo.android.BuildConfig
import com.chweibo.android.data.api.WeiboApiService
import com.chweibo.android.data.local.TokenDataStore
import com.chweibo.android.data.model.AccessToken
import com.chweibo.android.data.model.AuthConfig
import com.chweibo.android.data.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: WeiboApiService,
    private val tokenDataStore: TokenDataStore
) {
    companion object {
        const val CLIENT_SECRET = "3836abcf85a58fc94a65f2617828037a"  // 需要替换为实际的 App Secret
    }

    val isLoggedIn: Flow<Boolean> = tokenDataStore.isLoggedIn
    val accessToken: Flow<String?> = tokenDataStore.accessToken
    val userId: Flow<String?> = tokenDataStore.userId

    fun getAuthConfig(): AuthConfig {
        return AuthConfig(
            clientId = BuildConfig.WEIBO_APP_KEY,
            redirectUri = BuildConfig.WEIBO_REDIRECT_URI,
            scope = "email,direct_messages_read,direct_messages_write," +
                    "friendships_groups_read,friendships_groups_write," +
                    "statuses_to_me_read,follow_app_official_microblog," +
                    "invitation_write"
        )
    }

    suspend fun getAuthUrl(): String {
        return getAuthConfig().getAuthorizeUrl()
    }

    suspend fun handleAuthCallback(url: String): Result<AccessToken> {
        return try {
            // 从回调 URL 中提取 code
            val code = extractCodeFromUrl(url)
                ?: return Result.failure(Exception("无法从 URL 提取授权码"))

            // 使用 code 换取 access_token
            val response = apiService.getAccessToken(
                clientId = BuildConfig.WEIBO_APP_KEY,
                clientSecret = CLIENT_SECRET,
                code = code,
                redirectUri = BuildConfig.WEIBO_REDIRECT_URI
            )

            if (response.isSuccessful && response.body() != null) {
                val token = response.body()!!
                tokenDataStore.saveToken(token)
                Result.success(token)
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun refreshToken(): Result<AccessToken> {
        return try {
            val currentToken = tokenDataStore.accessToken.first()
                ?: return Result.failure(Exception("没有可用的 token"))

            val response = apiService.getTokenInfo(currentToken)
            if (response.isSuccessful && response.body() != null) {
                val tokenInfo = response.body()!!
                // Token 仍然有效
                Result.success(
                    AccessToken(
                        accessToken = currentToken,
                        expiresIn = tokenInfo.expireIn,
                        uid = tokenDataStore.userId.first()
                    )
                )
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout(): Result<Unit> {
        return try {
            val currentToken = tokenDataStore.accessToken.first()
            if (currentToken != null) {
                apiService.revokeToken(currentToken)
            }
            tokenDataStore.clearToken()
            Result.success(Unit)
        } catch (e: Exception) {
            // 即使 API 调用失败，也要清除本地 token
            tokenDataStore.clearToken()
            Result.success(Unit)
        }
    }

    suspend fun getCurrentUser(): Result<User> {
        return try {
            val uid = tokenDataStore.userId.first()?.toLongOrNull()
                ?: return Result.failure(Exception("用户未登录"))

            val response = apiService.getUserInfo(uid = uid)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun extractCodeFromUrl(url: String): String? {
        // 处理微博授权回调 URL
        // 格式: https://api.weibo.com/oauth2/default.html?code=CODE
        return try {
            val uri = android.net.Uri.parse(url)
            uri.getQueryParameter("code")
        } catch (e: Exception) {
            null
        }
    }
}
