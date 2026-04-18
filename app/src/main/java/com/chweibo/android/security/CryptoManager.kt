package com.chweibo.android.security

import android.content.Context
import android.util.Base64
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CryptoManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val aead: Aead

    init {
        AeadConfig.register()
        aead = AndroidKeysetManager.Builder()
            .withSharedPref(context, "auth_keyset", "auth_prefs")
            .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
            .withMasterKeyUri("android-keystore://weibo_auth_master_key")
            .build()
            .keysetHandle
            .getPrimitive(Aead::class.java)
    }

    fun encrypt(plaintext: String): String {
        val ciphertext = aead.encrypt(plaintext.toByteArray(Charsets.UTF_8), null)
        return Base64.encodeToString(ciphertext, Base64.DEFAULT)
    }

    fun decrypt(ciphertext: String): String {
        val decoded = Base64.decode(ciphertext, Base64.DEFAULT)
        return String(aead.decrypt(decoded, null), Charsets.UTF_8)
    }
}
