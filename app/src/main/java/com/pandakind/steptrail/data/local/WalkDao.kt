package com.pandakind.steptrail.data.local

import androidx.room.*
import com.pandakind.steptrail.data.local.entity.TrackPointEntity
import com.pandakind.steptrail.data.local.entity.WalkEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO для операций с прогулками и точками трека.
 */
@Dao
interface WalkDao {

    /**
     * Создаёт новую запись о прогулке.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWalk(walk: WalkEntity): Long

    /**
     * Обновляет существующую запись о прогулке.
     */
    @Update
    suspend fun updateWalk(walk: WalkEntity)

    /**
     * Удаляет запись о прогулке и все связанные точки.
     */
    @Delete
    suspend fun deleteWalk(walk: WalkEntity)

    /**
     * Возвращает Flow списка всех прогулок, отсортированный по дате (новые — первыми).
     */
    @Query("SELECT * FROM walks ORDER BY startDate DESC")
    fun getAllWalks(): Flow<List<WalkEntity>>

    /**
     * Возвращает Flow активной (не завершённой) прогулки.
     */
    @Query("SELECT * FROM walks WHERE isCompleted = 0 LIMIT 1")
    fun getCurrentWalk(): Flow<WalkEntity?>

    /**
     * Возвращает Flow прогулки по ID.
     */
    @Query("SELECT * FROM walks WHERE id = :walkId LIMIT 1")
    fun getWalkById(walkId: Long): Flow<WalkEntity?>

    /**
     * Удаляет самые старые прогулки, оставляя не более maxWalks.
     */
    @Query("DELETE FROM walks WHERE id NOT IN " +
            "(SELECT id FROM walks ORDER BY startDate DESC LIMIT :maxWalks)")
    suspend fun deleteOldestWalks(maxWalks: Int)

    /**
     * Возвращает количество прогулок.
     */
    @Query("SELECT COUNT(*) FROM walks")
    fun getWalkCount(): Flow<Int>

    // --- Точки трека ---

    /**
     * Сохраняет точку трека.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrackPoint(trackPoint: TrackPointEntity)

    /**
     * Удаляет все точки трека для данной прогулки.
     */
    @Query("DELETE FROM track_points WHERE walkId = :walkId")
    suspend fun deleteTrackPointsForWalk(walkId: Long)

    /**
     * Обновляет максимальную скорость для прогулки.
     */
    @Query("UPDATE walks SET maxSpeedMps = :maxSpeed WHERE id = :walkId")
    suspend fun setMaxSpeed(walkId: Long, maxSpeed: Double)
}
