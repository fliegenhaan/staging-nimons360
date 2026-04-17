package com.tubes.nimons360

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import com.tubes.nimons360.utils.NetworkMonitor
import okhttp3.OkHttpClient
import org.osmdroid.config.Configuration

class Nimons360App : Application() {

    val networkMonitor: NetworkMonitor by lazy { NetworkMonitor(this) }

    override fun onCreate() {
        super.onCreate()
        Configuration.getInstance().load(
            this,
            getSharedPreferences("osmdroid", MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = packageName
        createNotificationChannels()
        setupCoil()
    }

    private fun setupCoil() {
        SingletonImageLoader.setSafe {
            ImageLoader.Builder(this)
                .components {
                    add(OkHttpNetworkFetcherFactory(callFactory = { OkHttpClient() }))
                }
                .build()
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                LOCATION_CHANNEL_ID,
                "Pelacakan Lokasi",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifikasi untuk layanan berbagi lokasi real-time"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val LOCATION_CHANNEL_ID = "location_channel"
    }
}
