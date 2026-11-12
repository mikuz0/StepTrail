package com.pandakind.steptrail.util

import com.pandakind.steptrail.data.local.entity.TrackPointEntity
import com.pandakind.steptrail.data.local.entity.WalkEntity
import com.pandakind.steptrail.domain.model.TrackPoint
import com.pandakind.steptrail.domain.model.Walk

/**
 * Преобразует TrackPointEntity в доменную модель TrackPoint.
 */
fun TrackPointEntity.toDomain(): TrackPoint {
    return TrackPoint(
        walkId = walkId,
        latitude = latitude,
        longitude = longitude,
        accuracy = accuracy,
        timestamp = timestamp,
        speed = speed
    )
}

/**
 * Преобразует доменную модель TrackPoint в сущность TrackPointEntity.
 */
fun TrackPoint.toEntity(): TrackPointEntity {
    return TrackPointEntity(
        walkId = walkId,
        latitude = latitude,
        longitude = longitude,
        accuracy = accuracy,
        timestamp = timestamp,
        speed = speed
    )
}

/**
 * Преобразует WalkEntity в доменную модель Walk.
 */
fun WalkEntity.toDomain(): Walk {
    return Walk(
        id = id,
        startDate = startDate,
        durationSeconds = durationSeconds,
        distanceMeters = distanceMeters,
        avgSpeedMps = avgSpeedMps,
        maxSpeedMps = maxSpeedMps
    )
}
