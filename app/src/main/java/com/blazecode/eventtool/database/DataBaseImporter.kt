/*
 *
 *  * Copyright (c) BlazeCode / Ralf Lehmann, 2022.
 *
 */

package com.blazecode.eventtool.database

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.blazecode.eventtool.MainActivity
import com.blazecode.eventtool.R
import com.blazecode.eventtool.data.Event
import com.blazecode.eventtool.reminders.ReminderManager
import com.blazecode.eventtool.util.converter.LocalDateJsonTypeConverter
import com.blazecode.eventtool.util.converter.LocalTimeJsonTypeConverter
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

class DataBaseImporter(val activity: MainActivity) {

    fun importJson(){
        startIntent()
    }

    private fun startIntent(){
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS))
        }

        resultLauncher.launch(intent)
    }

    private var resultLauncher = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            if(data != null) parseData(Uri.parse(data.dataString))
        }
    }

    private fun parseData(uri: Uri){
        try {
            val jsonSelectedFile =  activity.contentResolver.openInputStream(uri)
            val inputAsString = jsonSelectedFile!!.bufferedReader().use { it.readText() }
            val gson = GsonBuilder()
                .registerTypeAdapter(LocalDate::class.java, LocalDateJsonTypeConverter().nullSafe())
                .registerTypeAdapter(LocalTime::class.java, LocalTimeJsonTypeConverter().nullSafe())
                .create()

            val eventarray: Array<Event> = gson.fromJson(inputAsString, Array<Event>::class.java)
            val eventList: MutableList<Event> = mutableListOf()
            for (event in eventarray){
                eventList.add(event)
            }
            resetAndWriteDatabase(eventList)
            activity.sendToast(activity.getString(R.string.import_successful))

        } catch (e: Exception) {
            activity.sendErrorDialog(e.toString().split("Exception: ")[2])
        }
    }

    private fun resetAndWriteDatabase(list: MutableList<Event>){
        val repository = EventRepository()

        activity.lifecycleScope.launch(Dispatchers.IO) {
            repository.clearDatabase(activity.applicationContext)

            for(event in list){
                repository.saveEvent(activity.applicationContext, event)
            }

            setAlarms(list)
        }
    }

    private fun setAlarms (list: MutableList<Event>) {
        val reminderManager = ReminderManager()
        reminderManager.scheduleAll(activity.applicationContext, list)
    }
}