/*
 *
 *  * Copyright (c) BlazeCode / Ralf Lehmann, 2022.
 *
 */

package com.blazecode.eventtool.data

import com.blazecode.eventtool.enums.EventType
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import java.time.LocalDate

data class EventDateType(
    val date: CalendarDay = CalendarDay(LocalDate.now(), DayPosition.MonthDate),
    val type: EventType = EventType.UNKNOWN
)
