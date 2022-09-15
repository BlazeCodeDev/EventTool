/*
 *
 *  * Copyright (c) BlazeCode / Ralf Lehmann, 2022.
 *
 */

package com.blazecode.eventtool.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Parcelable
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.blazecode.eventtool.MainActivity
import com.blazecode.eventtool.R
import com.blazecode.eventtool.data.Event
import com.blazecode.eventtool.enums.EventType

class NotificationManager {

    fun createReminderChannel(context: Context){
        // Create the NotificationChannel
        val name = context.getString(R.string.reminders_channel)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val mChannel = NotificationChannel(context.getString(R.string.REMINDERS_CHANNEL_ID), name, importance)
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        val systemNotificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        systemNotificationManager.createNotificationChannel(mChannel)
    }

    fun removeReminderChannel(context: Context){
        val systemNotificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        systemNotificationManager.deleteNotificationChannel(context.getString(R.string.REMINDERS_CHANNEL_ID))
    }

    fun postReminderNotification(context: Context, event: Event) {
        val name = if(event.eventType == EventType.WEDDING) "${event.firstName1} / ${event.firstName2} ${event.lastName}" else event.name
        val stringAdditions: MutableList<String> = mutableListOf()

        if(event.additions.size > 0){
            for (addition in event.additions){
                stringAdditions.add(context.resources.getString(context.resources.getIdentifier(addition.name.lowercase(), "string", context.packageName)))
            }
        }

        val tapIntent = Intent(context, MainActivity::class.java)
            .putExtra("event", event as Parcelable)
        val tapPendingIntent: PendingIntent? = TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(tapIntent)
            getPendingIntent(event.id!!, PendingIntent.FLAG_IMMUTABLE)
        }

        val builder = NotificationCompat.Builder(context, context.getString(R.string.reminders_channel))
            .setChannelId(context.getString(R.string.REMINDERS_CHANNEL_ID))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.resources.getString(R.string.notification_title, name, context.resources.getInteger(R.integer.REMINDER_AMOUNT_HOURS_BEFORE_TIME_READY)))
            .setContentText(stringAdditions.joinToString())
            .setStyle(NotificationCompat.BigTextStyle()
                    .bigText(stringAdditions.joinToString()))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(tapPendingIntent)

        with(NotificationManagerCompat.from(context)) {
            notify(event.id!!, builder.build())
        }
    }

    fun remove(context: Context, id: Int){
        NotificationManagerCompat.from(context).cancel(null, id)
    }
}