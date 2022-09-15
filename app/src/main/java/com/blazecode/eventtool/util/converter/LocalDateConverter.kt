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
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class LocalDateConverter {
    @TypeConverter
    fun toDate(dateString: String): LocalDate? {
        return LocalDate.parse(dateString)
    }

    @TypeConverter
    fun toDateString(date: LocalDate): String {
        return date.toString()
    }
}

class LocalDateJsonTypeConverter: TypeAdapter<LocalDate>() {

    override fun write(out: JsonWriter?, value: LocalDate?) {
        out!!.value(DateTimeFormatter.ISO_LOCAL_DATE.format(value))
    }

    override fun read(input: JsonReader?): LocalDate? {
        return LocalDate.parse(input!!.nextString())
    }
}