package com.pandakind.steptrail.domain.usecase

import com.pandakind.steptrail.domain.model.Walk
import com.pandakind.steptrail.domain.repository.WalkRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use Case для получения прогулки по ID.
 */
class GetWalkByIdUseCase(
    private val repository: WalkRepository
) {
    /**
     * Возвращает Flow прогулки с данным ID или null.
     */
    fun execute(id: Long): Flow<Walk?> = repository.getWalkById(id)
}
