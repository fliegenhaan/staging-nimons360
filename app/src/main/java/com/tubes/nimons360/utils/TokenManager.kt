package com.tubes.nimons360.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object TokenManager {
    private const val PREF_NAME = "nimons360_secure_prefs"
    private const val KEY_TOKEN = "auth_token"
    private const val KEY_USER_NAME = "user_name"

    @Volatile
    private var prefs: SharedPreferences? = null

    private fun getPrefs(context: Context): SharedPreferences {
        return prefs ?: synchronized(this) {
            prefs ?: buildPrefs(context.applicationContext).also { prefs = it }
        }
    }

    private fun buildPrefs(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            PREF_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveToken(context: Context, token: String) {
        getPrefs(context).edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(context: Context): String? {
        return getPrefs(context).getString(KEY_TOKEN, null)
    }

    fun clearToken(context: Context) {
        getPrefs(context).edit().remove(KEY_TOKEN).apply()
    }

    fun getBearerToken(context: Context): String {
        return "Bearer ${getToken(context)}"
    }

    fun isLoggedIn(context: Context): Boolean {
        return getToken(context) != null
    }

    fun saveUserName(context: Context, name: String) {
        getPrefs(context).edit().putString(KEY_USER_NAME, name).apply()
    }

    fun getUserName(context: Context): String? {
        return getPrefs(context).getString(KEY_USER_NAME, null)
    }
}