package com.pandakind.steptrail.ui.main

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pandakind.steptrail.data.location.LocationTracker
import com.pandakind.steptrail.domain.model.TrackPoint
import com.pandakind.steptrail.domain.repository.WalkRepository
import com.pandakind.steptrail.domain.usecase.StartWalkUseCase
import com.pandakind.steptrail.service.WalkTrackingService
import com.pandakind.steptrail.util.haversineDistance
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject
import kotlinx.coroutines.flow.update

/**
 * ViewModel главного экрана.
 * Управляет состоянием трекинга, работает с сервисом и repository.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val startWalkUseCase: StartWalkUseCase,
    private val repository: WalkRepository,
    private val locationTracker: LocationTracker
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var currentWalkId: Long = 0L
    private var lastLocationPoint: TrackPoint? = null
    private var totalDistanceMeters: Double = 0.0
    private var startTimeMillis: Long = 0L

    private var locationJob: Job? = null
    private var timerJob: Job? = null

    private companion object {
        const val TAG = "StepTrailVM"
    }

    /**
     * Запускает или останавливает прогулку.
     */
    fun toggleTracking() {
        if (_uiState.value.isTracking) {
            stopTracking()
        } else {
            startTracking()
        }
    }

    /**
     * Запускает новую прогулку.
     */
    private fun startTracking() {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d(TAG, "startTracking() called")
            currentWalkId = startWalkUseCase.execute()
            startTimeMillis = System.currentTimeMillis()
            totalDistanceMeters = 0.0
            lastLocationPoint = null

            startTrackingService()
            startTimer()
            subscribeToLocationUpdates()

            _uiState.update { state ->
                state.copy(
                    isTracking = true,
                    walkId = currentWalkId,
                    currentSpeedKmh = 0.0,
                    avgSpeedKmh = 0.0,
                    distanceKm = 0.0,
                    durationSeconds = 0,
                    gpsStatus = GpsStatus.SEARCHING
                )
            }
            Log.d(TAG, "startTracking() finished, walkId=$currentWalkId")
        }
    }

    /**
     * Запускает таймер обновления durationSeconds.
     */
    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                if (!_uiState.value.isTracking) break
                val elapsed = (System.currentTimeMillis() - startTimeMillis) / 1000
                _uiState.update { it.copy(durationSeconds = elapsed) }
                delay(1000)
            }
        }
    }

    /**
     * Останавливает прогулку.
     */
    private fun stopTracking() {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d(TAG, "stopTracking() called, totalDistance=$totalDistanceMeters")
            timerJob?.cancel()
            timerJob = null
            locationJob?.cancel()
            locationJob = null
            stopTrackingService()
            locationTracker.removeLocationUpdates()

            val durationSeconds = (System.currentTimeMillis() - startTimeMillis) / 1000
            val avgSpeedMps = if (durationSeconds > 0) totalDistanceMeters / durationSeconds else 0.0

            repository.finishWalk(
                walkId = currentWalkId,
                durationSeconds = durationSeconds,
                distanceMeters = totalDistanceMeters,
                avgSpeedMps = avgSpeedMps
            )

            _uiState.update { state ->
                state.copy(
                    isTracking = false,
                    currentSpeedKmh = 0.0,
                    avgSpeedKmh = 0.0,
                    distanceKm = totalDistanceMeters / 1000.0,
                    durationSeconds = durationSeconds
                )
            }
        }
    }

    /**
     * Запускает WalkTrackingService.
     */
    private fun startTrackingService() {
        val intent = Intent(context, WalkTrackingService::class.java).apply {
            action = WalkTrackingService.ACTION_START
            putExtra(WalkTrackingService.EXTRA_WALK_ID, currentWalkId)
        }
        ContextCompat.startForegroundService(context, intent)
    }

    /**
     * Останавливает WalkTrackingService.
     */
    private fun stopTrackingService() {
        val intent = Intent(context, WalkTrackingService::class.java).apply {
            action = WalkTrackingService.ACTION_STOP
        }
        context.startService(intent)
    }

    /**
     * Подписывается на обновления геолокации и обновляет UI.
     */
    private fun subscribeToLocationUpdates() {
        locationJob?.cancel()
        locationJob = viewModelScope.launch {
            Log.d(TAG, "Subscribed to locationFlow")
            locationTracker.locationFlow.collect { location ->
                Log.d(TAG, "VM got location: lat=${location.latitude}, speed=${location.speed}")

                // Обновляем статус GPS сразу
                _uiState.update { it.copy(gpsStatus = GpsStatus.ACTIVE) }

                // Обновляем текущую скорость
                val currentSpeedKmh = location.speed * 3.6
                _uiState.update { it.copy(currentSpeedKmh = currentSpeedKmh) }

                // Создаём точку
                val currentPoint = TrackPoint(
                    walkId = currentWalkId,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    accuracy = location.accuracy,
                    timestamp = Date(),
                    speed = location.speed
                )

                // Считаем расстояние от ПРЕДЫДУЩЕЙ точки
                val distance = lastLocationPoint?.let { last ->
                    haversineDistance(
                        last.latitude, last.longitude,
                        location.latitude, location.longitude
                    )
                } ?: 0.0

                // ВСЕГДА обновляем lastLocationPoint (даже если distance < 0.3)
                lastLocationPoint = currentPoint

                // Накапливаем дистанцию только если прошли >= 0.3 м
                if (distance >= 0.3) {
                    totalDistanceMeters += distance
                    Log.d(TAG, "Added distance: $distance, total=$totalDistanceMeters")

                    // Сохраняем точку в БД
                    repository.saveTrackPoint(currentPoint)

                    // Обновляем UI
                    val elapsed = (System.currentTimeMillis() - startTimeMillis) / 1000
                    val avgSpeed = if (elapsed > 0) totalDistanceMeters / elapsed else 0.0

                    _uiState.update { state ->
                        state.copy(
                            distanceKm = totalDistanceMeters / 1000.0,
                            avgSpeedKmh = avgSpeed * 3.6,
                            durationSeconds = elapsed
                        )
                    }
                } else {
                    Log.d(TAG, "Skipping: distance=$distance < 0.3")
                }
            }
        }
    }
}
