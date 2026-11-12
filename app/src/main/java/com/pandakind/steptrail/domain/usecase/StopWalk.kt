package com.pandakind.steptrail.domain.usecase

import com.pandakind.steptrail.domain.repository.WalkRepository

/**
 * Use Case для остановки текущей прогулки.
 */
class StopWalkUseCase(
    private val repository: WalkRepository
) {
    /**
     * Останавливает прогулку с данным ID.
     */
    suspend fun execute(walkId: Long) = repository.stopWalk(walkId)
}
