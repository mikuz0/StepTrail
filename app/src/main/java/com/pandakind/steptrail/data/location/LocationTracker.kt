package com.pandakind.steptrail.data.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
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
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val locationFlow: Flow<Location> = _locationFlow.asSharedFlow()

    private val _trackPointFlow = MutableSharedFlow<TrackPoint>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val trackPointFlow: Flow<TrackPoint> = _trackPointFlow.asSharedFlow()

    private var lastLocation: Location? = null
    private var lastTrackPoint: TrackPoint? = null

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            // Логируем КАЖДЫЙ вызов, даже если точек нет
            Log.d(TAG, "onLocationResult: ${result.locations.size} locations")
            for (location in result.locations.reversed()) {
                processLocation(location)
            }
        }
    }

    private companion object {
        const val TAG = "StepTrailGPS"
        // Порог фильтрации accuracy (в метрах)
        const val ACCURACY_THRESHOLD_M = 100f
        // Минимальное расстояние для эмита точки (в метрах)
        const val MIN_DISTANCE_M = 0.3
    }

    /**
     * Запрашивает обновления геолокации.
     */
    fun requestLocationUpdates() {
        val hasPerm = hasLocationPermission()
        Log.d(TAG, "requestLocationUpdates called, hasPermission=$hasPerm")
        if (!hasPerm) {
            Log.w(TAG, "Location permission not granted, aborting")
            return
        }

        // Ослаблены фильтры: интервал 5 сек, без фильтра по расстоянию,
        // без ожидания идеальной точности. Это убирает "blocked - too close"
        // и "blocked - too fast" в FusedLocation, из-за которых callback
        // не срабатывал, когда пользователь стоял на месте.
        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000L // интервал 5 секунд
        ).apply {
            setMinUpdateIntervalMillis(3000L)  // минимум 3 секунды
            setMinUpdateDistanceMeters(0f)     // не фильтровать по расстоянию
            setWaitForAccurateLocation(false)  // не ждать идеальной точности
        }.build()

        try {
            fusedClient.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper()
            )
            Log.d(TAG, "FusedLocation updates requested successfully")
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException in requestLocationUpdates", e)
        }
    }

    /**
     * Останавливает получение геолокации.
     */
    fun removeLocationUpdates() {
        Log.d(TAG, "removeLocationUpdates called")
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
     * Фильтрует по точности и минимальному расстоянию.
     */
    private fun processLocation(location: Location) {
        Log.d(
            TAG,
            "onLocation: lat=${location.latitude}, lon=${location.longitude}, " +
                    "speed=${location.speed}, acc=${location.accuracy}"
        )

        // Фильтр по точности: отбрасываем точки с accuracy > 100 м
        if (location.accuracy > ACCURACY_THRESHOLD_M) {
            Log.d(TAG, "Skipping: accuracy=${location.accuracy} > $ACCURACY_THRESHOLD_M")
            return
        }

        // ⬇️ ДОБАВИТЬ ЭТУ СТРОКУ
        _locationFlow.tryEmit(location)

        lastLocation = location

        // Вычисляем расстояние от последней точки
        val distanceMeters = lastTrackPoint?.let { tp ->
            haversineDistance(
                tp.latitude, tp.longitude,
                location.latitude, location.longitude
            )
        } ?: 0.0

        // Создаём точку трека
        val trackPoint = TrackPoint(
            walkId = 0L, // будет установлено сервисом
            latitude = location.latitude,
            longitude = location.longitude,
            accuracy = location.accuracy,
            timestamp = Date(),
            speed = location.speed
        )

        // lastTrackPoint обновляем ВСЕГДА после прохождения фильтра accuracy,
        // чтобы трек не "залипал" при медленной ходьбе
        lastTrackPoint = trackPoint

        // Эмитим точку если distanceMeters >= 0.3 м
        if (distanceMeters >= MIN_DISTANCE_M) {
            Log.d(TAG, "Emitting trackPoint: distance=$distanceMeters")
            _trackPointFlow.tryEmit(trackPoint)
        } else {
            Log.d(TAG, "Skipping: distance=$distanceMeters < $MIN_DISTANCE_M")
        }
    }
}