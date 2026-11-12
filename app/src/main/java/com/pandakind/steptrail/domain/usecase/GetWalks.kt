package com.pandakind.steptrail.domain.usecase

import com.pandakind.steptrail.domain.repository.WalkRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use Case для получения списка всех прогулок.
 */
class GetWalksUseCase(
    private val repository: WalkRepository
) {
    /**
     * Возвращает Flow списка всех прогулок (новые — первыми).
     */
    fun execute(): Flow<List<com.pandakind.steptrail.domain.model.Walk>> = repository.getWalks()
}
