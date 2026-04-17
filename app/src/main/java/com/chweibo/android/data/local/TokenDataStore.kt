package com.chweibo.android.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.chweibo.android.data.model.AccessToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "weibo_prefs")

@Singleton
class TokenDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore: DataStore<Preferences> = context.dataStore

    companion object {
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val EXPIRES_AT = longPreferencesKey("expires_at")
        val USER_ID = stringPreferencesKey("user_id")
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    }

    val accessToken: Flow<String?> = dataStore.data.map { preferences ->
        preferences[ACCESS_TOKEN]
    }

    val isLoggedIn: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[IS_LOGGED_IN] ?: false
    }

    val userId: Flow<String?> = dataStore.data.map { preferences ->
        preferences[USER_ID]
    }

    val expiresAt: Flow<Long> = dataStore.data.map { preferences ->
        preferences[EXPIRES_AT] ?: 0L
    }

    suspend fun saveToken(accessToken: AccessToken) {
        dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN] = accessToken.accessToken
            preferences[EXPIRES_AT] = accessToken.expiresAt
            preferences[USER_ID] = accessToken.uid ?: ""
            preferences[IS_LOGGED_IN] = true
        }
    }

    suspend fun clearToken() {
        dataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN)
            preferences.remove(REFRESH_TOKEN)
            preferences.remove(EXPIRES_AT)
            preferences.remove(USER_ID)
            preferences[IS_LOGGED_IN] = false
        }
    }

    suspend fun getCurrentToken(): AccessToken? {
        val prefs = dataStore.data.first()
        val token = prefs[ACCESS_TOKEN]
        return if (token != null) {
            AccessToken(
                accessToken = token,
                expiresIn = 0,
                uid = prefs[USER_ID],
                expiresAt = prefs[EXPIRES_AT] ?: 0
            )
        } else null
    }
}
