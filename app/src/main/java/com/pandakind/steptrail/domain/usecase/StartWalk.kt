package com.pandakind.steptrail.domain.usecase

import com.pandakind.steptrail.domain.repository.WalkRepository

/**
 * Use Case для запуска новой прогулки.
 */
class StartWalkUseCase(
    private val repository: WalkRepository
) {
    /**
     * Запускает новую прогулку и возвращает её ID.
     */
    suspend fun execute(): Long = repository.startWalk()
}
