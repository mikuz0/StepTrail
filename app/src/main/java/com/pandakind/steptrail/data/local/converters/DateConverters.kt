package com.pandakind.steptrail.data.local.converters

import androidx.room.TypeConverter
import java.util.Date

/**
 * Конвертеры типов для Room.
 * Преобразуют Date в Long (timestamp) и обратно.
 */
class DateConverters {

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}
