package com.pandakind.steptrail.data.repository

import com.pandakind.steptrail.data.local.WalkDao
import com.pandakind.steptrail.data.local.entity.WalkEntity
import com.pandakind.steptrail.domain.model.TrackPoint
import com.pandakind.steptrail.domain.model.Walk
import com.pandakind.steptrail.domain.repository.WalkRepository
import com.pandakind.steptrail.util.toDomain
import com.pandakind.steptrail.util.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Date

/**
 * Реализация репозитория прогулок на основе Room.
 */
class WalkRepositoryImpl(
    private val dao: WalkDao
) : WalkRepository {

    override suspend fun startWalk(): Long {
        // Удаляем предыдущую активную прогулку, если есть
        val current = dao.getCurrentWalk().first()
        current?.let {
            dao.deleteWalk(it)
        }

        val walk = WalkEntity(
            startDate = Date(),
            durationSeconds = 0,
            distanceMeters = 0.0,
            avgSpeedMps = 0.0,
            maxSpeedMps = 0.0,
            isCompleted = 0
        )
        return dao.insertWalk(walk)
    }

    override suspend fun stopWalk(walkId: Long) {
        val walk = dao.getWalkById(walkId).first() ?: return

        // Обновляем запись как завершённую
        val completedWalk = walk.copy(isCompleted = 1)
        dao.updateWalk(completedWalk)

        // Проверяем лимит в 10 прогулок
        val count = dao.getWalkCount().first()
        if (count > 10) {
            dao.deleteOldestWalks(10)
        }
    }

    override suspend fun saveTrackPoint(trackPoint: TrackPoint) {
        dao.insertTrackPoint(trackPoint.toEntity())
    }

    override fun getWalks(): Flow<List<Walk>> {
        return dao.getAllWalks().map { entities ->
            entities.filter { it.isCompleted == 1 }
                .map { it.toDomain() }
        }
    }

    override fun getCurrentWalk(): Flow<Walk?> {
        return dao.getCurrentWalk().map { entity ->
            entity?.toDomain()
        }
    }

    override fun getWalkById(id: Long): Flow<Walk?> {
        return dao.getWalkById(id).map { entity ->
            entity?.toDomain()
        }
    }
}
