/*
 *
 *  * Copyright (c) BlazeCode / Ralf Lehmann, 2022.
 *
 */

package com.blazecode.eventtool.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import android.util.Log
import com.blazecode.eventtool.R
import com.blazecode.eventtool.data.Event
import com.blazecode.eventtool.enums.EventType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class ReminderManager {
    private var alarmManager: AlarmManager? = null
    private lateinit var alarmIntent: PendingIntent

    fun scheduleAll(context: Context, list: MutableList<Event>){
        val HOURS_TO_SUBSTRACT: Long = context.resources.getInteger(R.integer.REMINDER_AMOUNT_HOURS_BEFORE_TIME_READY).toLong()
        val filteredList = mutableListOf<Event>()

        for (event in list) {
            if((event.date.isEqual(LocalDate.now()) && event.timeReady.minusHours(HOURS_TO_SUBSTRACT).isAfter(LocalTime.now()))
                || event.date.isAfter(LocalDate.now())
                && event.eventType != EventType.RESERVED) {
                filteredList.add(event)
            }
        }

        for(event in filteredList){
            schedule(context, event)
        }
    }

    fun schedule(context: Context, event: Event){
        val HOURS_TO_SUBSTRACT: Long = context.resources.getInteger(R.integer.REMINDER_AMOUNT_HOURS_BEFORE_TIME_READY).toLong()
        alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val id = if(event.id != null) event.id else 0
        alarmIntent = Intent(context, ReminderBroadcastReceiver::class.java)
            .putExtra("reminderEvent", event as Parcelable)
            .let { intent ->
            PendingIntent.getBroadcast(context, id!!, intent, PendingIntent.FLAG_IMMUTABLE)
        }
        Log.i("RECEIVER", "GAVE ${event}")

        alarmManager?.setExact(
            AlarmManager.RTC_WAKEUP,
            LocalDateTime.of(event.date, event.timeReady).minusHours(HOURS_TO_SUBSTRACT).atZone(ZoneId.systemDefault()).toEpochSecond() * 1000,
            alarmIntent
        )
    }
}