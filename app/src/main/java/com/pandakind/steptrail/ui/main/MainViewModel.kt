package com.pandakind.steptrail.ui.main

import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pandakind.steptrail.data.location.LocationTracker
import com.pandakind.steptrail.data.repository.WalkRepositoryImpl
import com.pandakind.steptrail.data.local.AppDatabase
import com.pandakind.steptrail.domain.usecase.GetWalksUseCase
import com.pandakind.steptrail.domain.usecase.ObserveCurrentWalkUseCase
import com.pandakind.steptrail.domain.usecase.StartWalkUseCase
import com.pandakind.steptrail.domain.usecase.StopWalkUseCase
import com.pandakind.steptrail.service.WalkTrackingService
import com.pandakind.steptrail.util.haversineDistance
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

/**
 * ViewModel главного экрана.
 * Управляет состоянием трекинга, работает с сервисом и repository.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val startWalkUseCase: StartWalkUseCase,
    private val stopWalkUseCase: StopWalkUseCase,
    private val observeCurrentWalkUseCase: ObserveCurrentWalkUseCase,
    private val getWalksUseCase: GetWalksUseCase,
    private val database: AppDatabase
) : ViewModel() {

    private val repository = WalkRepositoryImpl(database.walkDao())
    private val locationTracker = LocationTracker(context)

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var currentWalkId: Long = 0L
    private var lastLocationPoint: com.pandakind.steptrail.domain.model.TrackPoint? = null
    private var totalDistanceMeters: Double = 0.0
    private var startTimeMillis: Long = 0L

    private var serviceBound = false
    private var serviceConnection: ServiceConnection? = null

    // Счётчик для таймера длительности
    private val _timerTick = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    init {
        observeCurrentWalk()
        startTimerTick()
    }

    /**
     * Наблюдает за текущей активной прогулкой.
     */
    private fun observeCurrentWalk() {
        viewModelScope.launch {
            observeCurrentWalkUseCase.execute()
                .collect { walk ->
                    if (walk != null) {
                        currentWalkId = walk.id
                        _uiState.update { state ->
                            state.copy(
                                isTracking = true,
                                walkId = walk.id,
                                distanceKm = walk.distanceMeters / 1000.0,
                                avgSpeedKmh = walk.avgSpeedMps * 3.6,
                                durationSeconds = walk.durationSeconds
                            )
                        }
                        totalDistanceMeters = walk.distanceMeters
                    }
                }
        }
    }

    /**
     * Запускает поток тиков таймера (каждую секунду).
     */
    private fun startTimerTick() {
        viewModelScope.launch {
            while (true) {
                _timerTick.emit(Unit)
                kotlinx.coroutines.delay(1000)
            }
        }
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
            currentWalkId = startWalkUseCase.execute()
            startTimeMillis = System.currentTimeMillis()
            totalDistanceMeters = 0.0
            lastLocationPoint = null

            // Запускаем foreground service
            startTrackingService()

            // Подписываемся на обновления геолокации
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
        }
    }

    /**
     * Останавливает прогулку.
     */
    private fun stopTracking() {
        viewModelScope.launch(Dispatchers.IO) {
            // Останавливаем сервис
            stopTrackingService()

            // Останавливаем подписку на геолокацию
            locationTracker.removeLocationUpdates()

            // Сохраняем итоговые данные
            val duration = System.currentTimeMillis() - startTimeMillis
            val durationSeconds = duration / 1000
            val avgSpeed = if (durationSeconds > 0) totalDistanceMeters / durationSeconds else 0.0

            val walk = com.pandakind.steptrail.data.local.entity.WalkEntity(
                id = currentWalkId,
                startDate = Date(),
                durationSeconds = durationSeconds,
                distanceMeters = totalDistanceMeters,
                avgSpeedMps = avgSpeed,
                maxSpeedMps = 0.0,
                isCompleted = 1
            )
            database.walkDao().updateWalk(walk)
            repository.stopWalk(currentWalkId)

            _uiState.update { state ->
                state.copy(
                    isTracking = false,
                    currentSpeedKmh = 0.0,
                    avgSpeedKmh = 0.0,
                    durationSeconds = 0
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
        viewModelScope.launch {
            locationTracker.locationFlow.collect { location ->
                _uiState.update { state ->
                    state.copy(gpsStatus = GpsStatus.ACTIVE)
                }

                // Текущая скорость из GPS
                val currentSpeedKmh = location.speed * 3.6
                _uiState.update { state ->
                    state.copy(currentSpeedKmh = currentSpeedKmh)
                }

                // Вычисляем расстояние
                val currentPoint = com.pandakind.steptrail.domain.model.TrackPoint(
                    walkId = currentWalkId,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    accuracy = location.accuracy,
                    timestamp = Date(),
                    speed = location.speed
                )

                val distance = lastLocationPoint?.let { last ->
                    haversineDistance(
                        last.latitude, last.longitude,
                        location.latitude, location.longitude
                    )
                } ?: 0.0

                if (distance >= 0.5) {
                    totalDistanceMeters += distance
                    lastLocationPoint = currentPoint

                    // Сохраняем точку через сервис
                    val trackPoint = com.pandakind.steptrail.domain.model.TrackPoint(
                        walkId = currentWalkId,
                        latitude = location.latitude,
                        longitude = location.longitude,
                        accuracy = location.accuracy,
                        timestamp = Date(),
                        speed = location.speed
                    )
                    repository.saveTrackPoint(trackPoint)

                    // Обновляем UI
                    val elapsed = (System.currentTimeMillis() - startTimeMillis) / 1000
                    val avgSpeed = if (elapsed > 0) totalDistanceMeters / elapsed else 0.0

                    _uiState.update { state ->
                        state.copy(
                            distanceKm = totalDistanceMeters / 1000.0,
                            avgSpeedKmh = avgSpeed * 3.6,
                            durationSeconds = elapsed.toLong()
                        )
                    }
                }
            }
        }
    }
}
