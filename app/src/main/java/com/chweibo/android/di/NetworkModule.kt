package com.chweibo.android.di

import com.chweibo.android.BuildConfig
import com.chweibo.android.data.api.AuthInterceptor
import com.chweibo.android.data.api.TokenAuthenticator
import com.chweibo.android.data.api.WeiboApiService
import com.chweibo.android.data.local.SecureTokenDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideAuthInterceptor(tokenDataStore: SecureTokenDataStore): AuthInterceptor {
        return AuthInterceptor(tokenDataStore)
    }

    // Isolated client for token refresh to avoid circular dependency
    @Provides
    @Singleton
    @Named("refresh")
    fun provideRefreshOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @Named("refresh")
    fun provideRefreshRetrofit(@Named("refresh") okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.weibo.com/2/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @Named("refresh")
    fun provideRefreshApiService(@Named("refresh") retrofit: Retrofit): WeiboApiService {
        return retrofit.create(WeiboApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideTokenAuthenticator(
        tokenDataStore: SecureTokenDataStore,
        @Named("refresh") apiService: WeiboApiService
    ): TokenAuthenticator {
        return TokenAuthenticator(tokenDataStore, apiService)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .authenticator(tokenAuthenticator)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.HEADERS
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.weibo.com/2/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideWeiboApiService(retrofit: Retrofit): WeiboApiService {
        return retrofit.create(WeiboApiService::class.java)
    }
}
