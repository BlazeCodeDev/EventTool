/*
 *
 *  * Copyright (c) BlazeCode / Ralf Lehmann, 2022.
 *
 */

package com.blazecode.eventtool.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.blazecode.eventtool.database.EventRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class BootReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val reminderManager = ReminderManager()
        val repository = EventRepository()

        GlobalScope.launch {
            val list = repository.getEventList(context)
            reminderManager.scheduleAll(context, list)
        }
    }
}