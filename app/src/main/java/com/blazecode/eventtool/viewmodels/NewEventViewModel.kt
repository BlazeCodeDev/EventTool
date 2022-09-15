/*
 *
 *  * Copyright (c) BlazeCode / Ralf Lehmann, 2022.
 *
 */

package com.blazecode.eventtool.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.blazecode.eventtool.data.Event
import com.blazecode.eventtool.database.EventRepository
import com.blazecode.eventtool.enums.EventType
import com.blazecode.eventtool.reminders.ReminderManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NewEventViewModel(app: Application, val event: Event): AndroidViewModel(app) {
    val repository = EventRepository()
    val reminderManager = ReminderManager()
    var editMode = false

    init {
        if(event.id != null) editMode = true
    }

    fun changeEventType(newEventType: EventType){
        event.eventType = newEventType
    }

    fun saveEvent(){
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveEvent(getApplication(), event)
            reminderManager.schedule(getApplication(), event)
        }
    }

    fun deleteEvent(){
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteEvent(getApplication(), event.id!!)
        }
    }
}