package com.pandakind.steptrail.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Сущность прогулки в Room.
 */
@Entity(tableName = "walks")
data class WalkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startDate: Date,
    val durationSeconds: Long = 0,
    val distanceMeters: Double = 0.0,
    val avgSpeedMps: Double = 0.0,
    val maxSpeedMps: Double = 0.0,
    val isCompleted: Int = 0 // 0 = active, 1 = completed
)
