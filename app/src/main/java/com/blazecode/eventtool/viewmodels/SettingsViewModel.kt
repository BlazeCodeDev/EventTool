/*
 *
 *  * Copyright (c) BlazeCode / Ralf Lehmann, 2022.
 *
 */

package com.blazecode.eventtool.viewmodels

import android.app.Application
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.blazecode.eventtool.database.DataBaseExporter
import com.blazecode.eventtool.database.DataBaseImporter
import com.blazecode.eventtool.database.EventRepository
import com.blazecode.eventtool.util.DataStoreManager
import com.blazecode.eventtool.util.NotificationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class SettingsViewModel (app: Application): AndroidViewModel(app){

    val notificationManager = NotificationManager()
    val UPDATE_TAG = "update"

    // NOTIFICATIONS
    fun openNotificationSettings(){
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        intent.putExtra("android.provider.extra.APP_PACKAGE", getApplication<Application>().packageName);
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(getApplication(), intent, null)
    }

    // DATABASE
    fun exportDatabase(exporter: DataBaseExporter){
        val repository = EventRepository()

        viewModelScope.launch(Dispatchers.IO) {
            val list = repository.getEventList(getApplication())
            exporter.exportJson(getApplication(), list)
        }
    }

    fun importDatabase(importer: DataBaseImporter){
        importer.importJson()
    }

    // DATA STORE
    val dataStoreManager = DataStoreManager

    suspend fun setRemindersEnabled(context: Context, enabled: Boolean){
        dataStoreManager.setRemindersEnabled(context, enabled)
        if(enabled) {
            notificationManager.createReminderChannel(context)
        } else notificationManager.removeReminderChannel(context)
    }

    suspend fun getRemindersEnabled(context: Context): Boolean{
        return dataStoreManager.getRemindersEnabled(context)
    }

    suspend fun setColorfulDays(context: Context, enabled: Boolean){
        dataStoreManager.setColorfulDays(context, enabled)
    }

    suspend fun getColorfulDays(context: Context): Boolean{
        return dataStoreManager.getColorfulDays(context)
    }
}