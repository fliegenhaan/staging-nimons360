package com.tubes.nimons360.utils

object LocationState {
    @Volatile var currentAzimuth: Float = 0f
    @Volatile var currentLat: Double = 0.0
    @Volatile var currentLon: Double = 0.0
    @Volatile var cachedUserName: String = ""
}
