package com.chweibo.android.data.api

import com.chweibo.android.data.local.SecureTokenDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenDataStore: SecureTokenDataStore
) : Interceptor {

    companion object {
        private const val TAG = "AuthInterceptor"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val originalUrl = originalRequest.url
        val encodedPath = originalUrl.encodedPath

        android.util.Log.d(TAG, "Intercepting request: $encodedPath")

        if (originalUrl.queryParameter("access_token") != null) {
            android.util.Log.d(TAG, "Request already has access_token")
            return chain.proceed(originalRequest)
        }

        val token = runBlocking {
            tokenDataStore.accessToken.first()
        }

        android.util.Log.d(TAG, "Token present: ${!token.isNullOrEmpty()}")

        val isOAuthRequest = encodedPath.contains("/oauth2/")
        if (token.isNullOrEmpty()) {
            if (!isOAuthRequest) {
                android.util.Log.e(TAG, "Missing access_token for request: $encodedPath")
                throw IOException("Missing access_token for request: $encodedPath")
            }
            return chain.proceed(originalRequest)
        }

        val newUrl = originalUrl.newBuilder()
            .addQueryParameter("access_token", token)
            .build()

        val newRequest = originalRequest.newBuilder()
            .url(newUrl)
            .build()

        val maskedUrl = newUrl.toString().replace(token, "TOKEN_HIDDEN")
        android.util.Log.d(TAG, "Request URL: $maskedUrl")

        val response = chain.proceed(newRequest)
        android.util.Log.d(TAG, "Response code: ${response.code} for $encodedPath")

        return response
    }
}
