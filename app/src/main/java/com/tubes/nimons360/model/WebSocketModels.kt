package com.tubes.nimons360.model

import com.google.gson.JsonElement

data class WebSocketOutMessage(
    val type: String,
    val payload: PresencePayload,
    val timestamp: String
)

data class PresencePayload(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val rotation: Float,
    val batteryLevel: Int,
    val isCharging: Boolean,
    val internetStatus: String,
    val metadata: Map<String, Any> = emptyMap()
)

data class WebSocketInMessage(
    val type: String,
    val payload: JsonElement?,
    val timestamp: String?
)

data class MemberPresence(
    val userId: Int,
    val email: String,
    val fullName: String,
    val latitude: Double,
    val longitude: Double,
    val rotation: Float,
    val batteryLevel: Int,
    val isCharging: Boolean,
    val internetStatus: String
)
