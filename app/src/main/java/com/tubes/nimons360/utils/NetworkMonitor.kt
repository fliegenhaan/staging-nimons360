package com.tubes.nimons360.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NetworkMonitor(private val context: Context) {

    private val _isOnline = MutableStateFlow(NetworkUtils.isConnected(context))
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val connectivityManager =
        context.getSystemService(ConnectivityManager::class.java)

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _isOnline.value = true
        }

        override fun onLost(network: Network) {
            _isOnline.value = NetworkUtils.isConnected(context)
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            _isOnline.value = networkCapabilities.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_INTERNET
            )
        }
    }

    fun start() {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        try {
            connectivityManager?.registerNetworkCallback(request, networkCallback)
        } catch (_: Exception) {
        }
    }

    fun stop() {
        try {
            connectivityManager?.unregisterNetworkCallback(networkCallback)
        } catch (_: Exception) {
        }
    }
}
