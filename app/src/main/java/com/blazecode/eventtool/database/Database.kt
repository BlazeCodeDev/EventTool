/*
 *
 *  * Copyright (c) BlazeCode / Ralf Lehmann, 2022.
 *
 */

package com.blazecode.eventtool.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.blazecode.eventtool.data.Event
import com.blazecode.eventtool.util.converter.AdditionsTypeConverter
import com.blazecode.eventtool.util.converter.LocalDateConverter
import com.blazecode.eventtool.util.converter.LocalTimeConverter

@androidx.room.Database(
    entities = [Event::class],
    version = 1,
    exportSchema = true)

@TypeConverters(LocalDateConverter::class, LocalTimeConverter::class, AdditionsTypeConverter::class)
abstract class Database: RoomDatabase() {
    abstract fun eventDao(): EventDAO

    companion object {
        fun build(context: Context) = Room.databaseBuilder(context, Database::class.java, "database-events")
            .build()
    }
}