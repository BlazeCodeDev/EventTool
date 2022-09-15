/*
 *
 *  * Copyright (c) BlazeCode / Ralf Lehmann, 2022.
 *
 */

package com.blazecode.eventtool.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.blazecode.eventtool.data.Event
import com.blazecode.eventtool.util.DataStoreManager
import com.blazecode.eventtool.util.NotificationManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ReminderBroadcastReceiver: BroadcastReceiver() {
    val notificationManager = NotificationManager()

    override fun onReceive(context: Context, intent: Intent) {
        val manager = DataStoreManager

        GlobalScope.launch {
            if(manager.getRemindersEnabled(context))
                intent.extras?.getParcelable("reminderEvent", Event::class.java)?.let { notificationManager.postReminderNotification(context!!, it) }
        }
    }
}