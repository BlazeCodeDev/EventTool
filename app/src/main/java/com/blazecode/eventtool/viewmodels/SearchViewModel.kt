/*
 *
 *  * Copyright (c) BlazeCode / Ralf Lehmann, 2023.
 *
 */

package com.blazecode.eventtool.viewmodels

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.blazecode.eventtool.data.Event
import com.blazecode.eventtool.database.EventRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SearchViewModel (app: Application): AndroidViewModel(app){
    val repository = EventRepository()

    var searchText = mutableStateOf("")
    private var searchResults = mutableListOf<Event>()

    suspend fun searchEvents(): MutableList<Event> {
        if(searchText.value.isEmpty()){
            viewModelScope.launch(Dispatchers.IO) {
                searchResults = repository.getEventList(getApplication())
            }.join()
        } else {
            viewModelScope.launch(Dispatchers.IO) {
                searchResults = repository.getEventsByName(getApplication(), searchText.value)
            }.join()
        }
        return searchResults
    }
}