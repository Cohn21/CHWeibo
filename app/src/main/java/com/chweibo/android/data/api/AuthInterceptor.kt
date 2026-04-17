package com.chweibo.android.data.api

import com.chweibo.android.data.local.TokenDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenDataStore: TokenDataStore
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val originalUrl = originalRequest.url

        // 如果请求已经包含 access_token，直接发送
        if (originalUrl.queryParameter("access_token") != null) {
            return chain.proceed(originalRequest)
        }

        // 获取保存的 token
        val token = runBlocking {
            tokenDataStore.accessToken.first()
        }

        // 如果没有 token，直接发送请求（某些接口不需要认证）
        if (token.isNullOrEmpty()) {
            return chain.proceed(originalRequest)
        }

        // 添加 access_token 到请求
        val newUrl = originalUrl.newBuilder()
            .addQueryParameter("access_token", token)
            .build()

        val newRequest = originalRequest.newBuilder()
            .url(newUrl)
            .build()

        return chain.proceed(newRequest)
    }
}
