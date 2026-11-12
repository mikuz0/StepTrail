package com.pandakind.steptrail.domain.model

import java.util.Date

/**
 * Сущность прогулки.
 * Содержит агрегированные данные о завершённой прогулке.
 */
data class Walk(
    val id: Long,
    val startDate: Date,
    val durationSeconds: Long,
    val distanceMeters: Double,
    val avgSpeedMps: Double,
    val maxSpeedMps: Double
)
