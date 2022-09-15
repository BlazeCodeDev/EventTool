/*
 *
 *  * Copyright (c) BlazeCode / Ralf Lehmann, 2022.
 *
 */

package com.blazecode.eventtool.database

import android.content.Context
import androidx.room.Room
import com.blazecode.eventtool.data.Event

class EventRepository {

    fun saveEvent(context: Context, event: Event){
        val db = Room.databaseBuilder(context, Database::class.java, "database-events").build()
        val dao = db.eventDao()
        dao.addEvent(event)
        db.close()
    }

    fun deleteEvent(context: Context, eventId: Int){
        val db = Room.databaseBuilder(context, Database::class.java, "database-events").build()
        val dao = db.eventDao()
        dao.deleteById(eventId)
        db.close()
    }

    fun getEventList(context: Context): MutableList<Event> {
        var tempList = mutableListOf<Event>()
        val db = Room.databaseBuilder(context, Database::class.java, "database-events").build()
        val dao = db.eventDao()

        tempList = dao.getAll()

        db.close()

        return tempList
    }

    fun clearDatabase(context: Context){
        val db = Room.databaseBuilder(context, Database::class.java, "database-events").build()
        val dao = db.eventDao()
        dao.clear()
        db.close()
    }
}