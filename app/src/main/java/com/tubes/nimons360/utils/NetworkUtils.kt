package com.tubes.nimons360.utils

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.tubes.nimons360.ui.auth.LoginActivity

object NetworkUtils {

    fun isConnected(context: Context): Boolean {
        val cm = context.getSystemService(ConnectivityManager::class.java) ?: return false
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun handleExpiredToken(context: Context) {
        TokenManager.clearToken(context)
        val intent = Intent(context, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        context.startActivity(intent)
    }
}
