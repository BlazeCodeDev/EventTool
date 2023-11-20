/*
 *
 *  * Copyright (c) BlazeCode / Ralf Lehmann, 2023.
 *
 */

package com.blazecode.eventtool.reminders

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import android.provider.Settings
import android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
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
                || event.date.isAfter(LocalDate.now())) {
                filteredList.add(event)
            }
        }

        for(event in filteredList){
            schedule(context, event)
        }
    }

    fun schedule(context: Context, event: Event){
        if(event.eventType != EventType.RESERVED) {
            val HOURS_TO_SUBSTRACT: Long = context.resources.getInteger(R.integer.REMINDER_AMOUNT_HOURS_BEFORE_TIME_READY).toLong()
            alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val id = if(event.id != null) event.id else 0
            alarmIntent = Intent(context, ReminderBroadcastReceiver::class.java)
                .putExtra("reminderEvent", event as Parcelable)
                .let { intent ->
                    PendingIntent.getBroadcast(context, id!!, intent, PendingIntent.FLAG_IMMUTABLE)
                }
            Log.i("RECEIVER", "GAVE ${event}")

            if(alarmManager?.canScheduleExactAlarms() == true){
                alarmManager?.setExact(
                    AlarmManager.RTC_WAKEUP,
                    LocalDateTime.of(event.date, event.timeReady).minusHours(HOURS_TO_SUBSTRACT).atZone(ZoneId.systemDefault()).toEpochSecond() * 1000,
                    alarmIntent
                )
            } else {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(context, intent, null)
            }
        }
    }
}