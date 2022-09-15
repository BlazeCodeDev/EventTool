/*
 *
 *  * Copyright (c) BlazeCode / Ralf Lehmann, 2022.
 *
 */

package com.blazecode.eventtool.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

object DataStoreManager {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "preferences")
    private val REMINDERS = booleanPreferencesKey("reminders")
    private val COLORFUL_CALENDAR_DAYS = booleanPreferencesKey("colorfulCalendarDays")

    private val DEBUG_UPDATE_CHECK = booleanPreferencesKey("debugUpdateCheck")

    suspend fun setRemindersEnabled(context: Context, enabled: Boolean){
        context.dataStore.edit { settings ->
            settings[REMINDERS] = enabled
        }
    }

    suspend fun getRemindersEnabled(context: Context): Boolean{
        val preferences = context.dataStore.data.first()
        preferences[REMINDERS]?.let {
            return  it
        }
        return false
    }

    suspend fun setColorfulDays(context: Context, enabled: Boolean){
        context.dataStore.edit { settings ->
            settings[COLORFUL_CALENDAR_DAYS] = enabled
        }
    }

    suspend fun getColorfulDays(context: Context): Boolean{
        val preferences = context.dataStore.data.first()
        preferences[COLORFUL_CALENDAR_DAYS]?.let {
            return  it
        }
        return false
    }

    // DEBUG MENU
    suspend fun setDebugUpdateCheck(context: Context, enabled: Boolean){
        context.dataStore.edit { settings ->
            settings[DEBUG_UPDATE_CHECK] = enabled
        }
    }

    suspend fun getDebugUpdateCheck(context: Context): Boolean{
        val preferences = context.dataStore.data.first()
        preferences[DEBUG_UPDATE_CHECK]?.let {
            return  it
        }
        return false
    }
}