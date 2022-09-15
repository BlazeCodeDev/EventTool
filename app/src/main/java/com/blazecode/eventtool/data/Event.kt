/*
 *
 *  * Copyright (c) BlazeCode / Ralf Lehmann, 2022.
 *
 */

package com.blazecode.eventtool.data

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.blazecode.eventtool.enums.Additions
import com.blazecode.eventtool.enums.EventType
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.time.LocalTime

@Entity
@Parcelize
data class Event(
    @PrimaryKey(autoGenerate = true) var id: Int?,
    @ColumnInfo var eventType: EventType = EventType.UNKNOWN,
    @ColumnInfo var date: LocalDate = LocalDate.now(),
    @ColumnInfo var name: String = "",
    @ColumnInfo var firstName1: String = "",
    @ColumnInfo var firstName2: String = "",
    @ColumnInfo var lastName: String = "",
    @ColumnInfo var venue: String = "",
    @ColumnInfo var timeReady: LocalTime = LocalTime.of(LocalTime.now().hour, 0),
    @ColumnInfo var timeStart: LocalTime = LocalTime.of(LocalTime.now().hour, 0),
    @ColumnInfo var timeGuests: LocalTime = LocalTime.of(LocalTime.now().hour, 0),
    @ColumnInfo var timeEnd: LocalTime = LocalTime.of(LocalTime.now().hour, 0),
    @ColumnInfo var guestAmount: String = "0",
    @ColumnInfo var childrenAmount: String = "0",
    @ColumnInfo var additions: MutableList<Additions> = mutableListOf(),
    @ColumnInfo var wishMusic: String = "",
    @ColumnInfo var comments: String = "",
    @ColumnInfo var email: String = "",
    @ColumnInfo var phone: String = ""
) : Parcelable
