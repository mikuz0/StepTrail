package com.pandakind.steptrail.ui.main

/**
 * Состояние UI главного экрана.
 */
data class MainUiState(
    val isTracking: Boolean = false,
    val currentSpeedKmh: Double = 0.0,
    val avgSpeedKmh: Double = 0.0,
    val distanceKm: Double = 0.0,
    val durationSeconds: Long = 0,
    val gpsStatus: GpsStatus = GpsStatus.NO_PERMISSION,
    val walkId: Long = 0L
)

/**
 * Статус GPS-сигнала.
 */
enum class GpsStatus {
    NO_PERMISSION,
    SEARCHING,
    ACTIVE
}
