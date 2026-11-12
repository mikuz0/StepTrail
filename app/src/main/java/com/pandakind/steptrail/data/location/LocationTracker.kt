package com.pandakind.steptrail.data.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.pandakind.steptrail.domain.model.TrackPoint
import com.pandakind.steptrail.util.haversineDistance
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.Date

/**
 * Обёртка над FusedLocationProviderClient для получения GPS-координат.
 * Фильтрует точки с низкой точностью и неподвижного пользователя.
 */
class LocationTracker(
    private val context: Context
) {

    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    private val _locationFlow = MutableSharedFlow<Location>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val locationFlow: Flow<Location> = _locationFlow.asSharedFlow()

    private val _trackPointFlow = MutableSharedFlow<TrackPoint>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val trackPointFlow: Flow<TrackPoint> = _trackPointFlow.asSharedFlow()

    private var lastLocation: Location? = null
    private var lastTrackPoint: TrackPoint? = null

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            for (location in result.locations.reversed()) {
                processLocation(location)
            }
        }
    }

    /**
     * Запрашивает обновления геолокации.
     */
    fun requestLocationUpdates() {
        if (!hasLocationPermission()) return

        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            2000L // интервал 2 секунды
        ).apply {
            setMinUpdateIntervalMillis(1000L)
            setMinUpdateDistanceMeters(1f)
        }.build()

        fusedClient.requestLocationUpdates(
            request,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    /**
     * Останавливает получение геолокации.
     */
    fun removeLocationUpdates() {
        fusedClient.removeLocationUpdates(locationCallback)
        lastLocation = null
        lastTrackPoint = null
    }

    /**
     * Возвращает последнюю известную геопозицию (если есть).
     */
    fun getLastLocation(): Location? {
        return lastLocation
    }

    /**
     * Проверяет наличие разрешения на геолокацию.
     */
    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Обрабатывает полученную точку геолокации.
     * Фильтрует по точности и скорости движения.
     */
    private fun processLocation(location: Location) {
        // Фильтр по точности: отбрасываем точки с accuracy > 30 м
        if (location.accuracy > 30f) return

        lastLocation = location

        // Фильтр неподвижного пользователя: скорость < 0.3 м/с
        val speedMs = location.speed
        if (speedMs < 0.3f) return

        // Вычисляем расстояние от последней точки
        val distanceMeters = lastTrackPoint?.let { tp ->
            haversineDistance(
                tp.latitude, tp.longitude,
                location.latitude, location.longitude
            )
        } ?: 0.0

        // Точки, где пользователь не двигался (расстояние ~0), не увеличивают дистанцию
        if (distanceMeters < 0.5) return

        val trackPoint = TrackPoint(
            walkId = 0L, // будет установлено сервисом
            latitude = location.latitude,
            longitude = location.longitude,
            accuracy = location.accuracy,
            timestamp = Date(),
            speed = location.speed
        )
        lastTrackPoint = trackPoint
        _trackPointFlow.tryEmit(trackPoint)
    }
}
