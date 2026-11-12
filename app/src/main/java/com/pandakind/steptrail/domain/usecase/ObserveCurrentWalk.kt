package com.pandakind.steptrail.domain.usecase

import com.pandakind.steptrail.domain.repository.WalkRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use Case для наблюдения за текущей активной прогулкой.
 */
class ObserveCurrentWalkUseCase(
    private val repository: WalkRepository
) {
    /**
     * Возвращает Flow текущей активной прогулки или null.
     */
    fun execute(): Flow<com.pandakind.steptrail.domain.model.Walk?> = repository.getCurrentWalk()
}
