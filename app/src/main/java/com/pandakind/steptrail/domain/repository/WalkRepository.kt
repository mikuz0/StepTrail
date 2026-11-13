package com.pandakind.steptrail.domain.repository

import com.pandakind.steptrail.domain.model.TrackPoint
import com.pandakind.steptrail.domain.model.Walk
import kotlinx.coroutines.flow.Flow

/**
 * Интерфейс репозитория для хранения и получения данных о прогулках.
 */
interface WalkRepository {

    /**
     * Начинает новую прогулку. Возвращает ID созданной записи.
     */
    suspend fun startWalk(): Long

    /**
     * Останавливает прогулку с данным ID.
     */
    suspend fun stopWalk(walkId: Long)

    /**
     * Завершает прогулку с расчётом длительности и средней скорости.
     */
    suspend fun finishWalk(
        walkId: Long,
        durationSeconds: Long,
        distanceMeters: Double,
        avgSpeedMps: Double
    )

    /**
     * Обновляет максимальную скорость прогулки.
     */
    suspend fun updateMaxSpeed(walkId: Long, maxSpeedMps: Double)

    /**
     * Сохраняет точку трека.
     */
    suspend fun saveTrackPoint(trackPoint: TrackPoint)

    /**
     * Наблюдает за списком всех прогулок (новые — первыми).
     */
    fun getWalks(): Flow<List<Walk>>

    /**
     * Наблюдает за текущей активной прогулкой (если есть).
     */
    fun getCurrentWalk(): Flow<Walk?>

    /**
     * Получает прогулку по ID.
     */
    fun getWalkById(id: Long): Flow<Walk?>
}
