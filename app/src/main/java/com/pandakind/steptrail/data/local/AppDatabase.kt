package com.pandakind.steptrail.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.pandakind.steptrail.data.local.converters.DateConverters
import com.pandakind.steptrail.data.local.entity.TrackPointEntity
import com.pandakind.steptrail.data.local.entity.WalkEntity

/**
 * База данных Room для хранения прогулок и точек трека.
 */
@Database(
    entities = [WalkEntity::class, TrackPointEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun walkDao(): WalkDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Получает синглтон-экземпляр базы данных.
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "steptrail_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
