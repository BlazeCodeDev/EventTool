/*
 *
 *  * Copyright (c) BlazeCode / Ralf Lehmann, 2022.
 *
 */

package com.blazecode.eventtool.util.converter

import androidx.room.TypeConverter
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class LocalTimeConverter {
    @TypeConverter
    fun toTime(timeString: String): LocalTime {
        return LocalTime.parse(timeString)
    }

    @TypeConverter
    fun toTimeString(time: LocalTime): String {
        return time.toString()
    }
}

class LocalTimeJsonTypeConverter: TypeAdapter<LocalTime>() {

    override fun write(out: JsonWriter?, value: LocalTime?) {
        out!!.value(DateTimeFormatter.ISO_LOCAL_TIME.format(value))
    }

    override fun read(input: JsonReader?): LocalTime? {
        return LocalTime.parse(input!!.nextString())
    }
}