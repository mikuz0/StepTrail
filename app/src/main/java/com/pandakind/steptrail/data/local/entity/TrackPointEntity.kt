package com.pandakind.steptrail.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Сущность точки трека в Room.
 * Связь 1:N с WalkEntity через foreignKey.
 */
@Entity(
    tableName = "track_points",
    foreignKeys = [
        ForeignKey(
            entity = WalkEntity::class,
            parentColumns = ["id"],
            childColumns = ["walkId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("walkId")]
)
data class TrackPointEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val walkId: Long,
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val timestamp: Date,
    val speed: Float
)
