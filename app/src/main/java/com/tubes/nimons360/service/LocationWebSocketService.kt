package com.tubes.nimons360.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.gson.Gson
import com.tubes.nimons360.MainActivity
import com.tubes.nimons360.Nimons360App
import com.tubes.nimons360.model.MemberPresence
import com.tubes.nimons360.model.PresencePayload
import com.tubes.nimons360.model.WebSocketInMessage
import com.tubes.nimons360.model.WebSocketOutMessage
import com.tubes.nimons360.utils.LocationState
import com.tubes.nimons360.utils.TokenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.logging.HttpLoggingInterceptor
import java.time.Instant
import java.util.concurrent.TimeUnit

class LocationWebSocketService : Service() {

    private val supervisorJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + supervisorJob)
    private val gson = Gson()
    private var webSocket: WebSocket? = null
    private var retryCount = 0
    private val lastUpdateMap = mutableMapOf<Int, Long>()

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.SECONDS) // no timeout for websocket
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            })
            .build()
    }

    companion object {
        private const val TAG = "LocationWebSocketSvc"
        const val NOTIF_ID = 1001

        val presenceUpdates = MutableSharedFlow<MemberPresence>(
            replay = 0,
            extraBufferCapacity = 64,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )

        var isRunning = false
    }

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        startForeground(NOTIF_ID, buildNotification())
        createWebSocket()
        startPresenceLoop()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        webSocket?.close(1000, "Service stopped")
        supervisorJob.cancel()
    }

    private fun createWebSocket() {
        val token = TokenManager.getBearerToken(this)
        val request = Request.Builder()
            .url("wss://mad.labpro.hmif.dev/ws/live")
            .header("Authorization", token)
            .build()

        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "WebSocket connected")
                retryCount = 0
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                handleIncomingMessage(text)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "WebSocket failure: ${t.message}")
                scheduleReconnect()
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket closed: $reason")
                if (code != 1000) scheduleReconnect()
            }
        })
    }

    private fun scheduleReconnect() {
        if (retryCount >= 5) {
            Log.w(TAG, "Max retries reached, stopping reconnect attempts")
            return
        }
        retryCount++
        serviceScope.launch {
            delay(5000L * retryCount)
            if (isRunning) createWebSocket()
        }
    }

    private fun startPresenceLoop() {
        serviceScope.launch {
            while (isActive) {
                val userName = TokenManager.getUserName(this@LocationWebSocketService)
                    ?: LocationState.cachedUserName

                val battery = getBatteryInfo()
                val payload = PresencePayload(
                    name = userName,
                    latitude = LocationState.currentLat,
                    longitude = LocationState.currentLon,
                    rotation = LocationState.currentAzimuth,
                    batteryLevel = battery.first,
                    isCharging = battery.second,
                    internetStatus = getInternetStatus()
                )
                val message = WebSocketOutMessage(
                    type = "update_presence",
                    payload = payload,
                    timestamp = Instant.now().toString()
                )
                webSocket?.send(gson.toJson(message))
                delay(1000L)
            }
        }
    }

    private fun handleIncomingMessage(text: String) {
        try {
            val msg = gson.fromJson(text, WebSocketInMessage::class.java)
            when (msg.type) {
                "member_presence_updated" -> {
                    val presence = gson.fromJson(msg.payload, MemberPresence::class.java)
                    lastUpdateMap[presence.userId] = System.currentTimeMillis()
                    serviceScope.launch {
                        presenceUpdates.emit(presence)
                    }
                }
                "pong" -> { /* heartbeat ack */ }
                else -> Log.d(TAG, "Unhandled message type: ${msg.type}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing message: ${e.message}")
        }
    }

    private fun getBatteryInfo(): Pair<Int, Boolean> {
        val batteryStatus = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val pct = if (level >= 0 && scale > 0) (level * 100 / scale) else 0
        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL
        return Pair(pct, isCharging)
    }

    private fun getInternetStatus(): String {
        val cm = getSystemService(ConnectivityManager::class.java) ?: return "mobile"
        val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return "mobile"
        return if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) "wifi" else "mobile"
    }

    private fun buildNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, Nimons360App.LOCATION_CHANNEL_ID)
            .setContentTitle("Nimons360")
            .setContentText("Berbagi lokasi aktif")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}
