package com.pandakind.steptrail.domain.model

import java.util.Date

/**
 * Точка трека — GPS-координата, записанная во время прогулки.
 */
data class TrackPoint(
    val walkId: Long,
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val timestamp: Date,
    val speed: Float
)
