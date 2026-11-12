package com.pandakind.steptrail.util

/**
 * Вычисляет расстояние по формуле Haversine между двумя GPS-точками.
 *
 * @param lat1 Широта первой точки в градусах.
 * @param lon1 Долгота первой точки в градусах.
 * @param lat2 Широта второй точки в градусах.
 * @param lon2 Долгота второй точки в градусах.
 * @return Расстояние в метрах.
 */
fun haversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val earthRadius = 6371000.0 // метры

    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)

    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2)

    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

    return earthRadius * c
}

/**
 * Вычисляет скорость на основе расстояния и времени.
 *
 * @param distanceMeters Расстояние в метрах.
 * @param durationSeconds Длительность в секундах.
 * @return Скорость в м/с.
 */
fun calculateSpeed(distanceMeters: Double, durationSeconds: Long): Double {
    return if (durationSeconds > 0) distanceMeters / durationSeconds else 0.0
}
