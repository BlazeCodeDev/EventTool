/*
 *
 *  * Copyright (c) BlazeCode / Ralf Lehmann, 2023.
 *
 */

package com.blazecode.eventtool.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.blazecode.eventtool.data.Event
import com.blazecode.eventtool.database.EventRepository
import com.blazecode.eventtool.util.DataStoreManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeViewModel(app: Application): AndroidViewModel(app) {
    val repository = EventRepository()

    suspend fun getEventList(): MutableList<Event>{
        var tempList = mutableListOf<Event>()
        viewModelScope.launch(Dispatchers.IO) {
            tempList = repository.getEventList(getApplication())
        }.join()
        return tempList
    }

    suspend fun setDebugUpdateCheck(context: Context, enabled: Boolean){
        val manager = DataStoreManager
        return manager.setDebugUpdateCheck(context, enabled)
    }

    suspend fun getDebugUpdateCheck(context: Context): Boolean{
        val manager = DataStoreManager
        return manager.getDebugUpdateCheck(context)
    }

    suspend fun getColorfulDays(context: Context): Boolean{
        val manager = DataStoreManager
        return manager.getColorfulDays(context)
    }
}
