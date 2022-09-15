/*
 *
 *  * Copyright (c) BlazeCode / Ralf Lehmann, 2022.
 *
 */

package com.blazecode.eventtool.database

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import androidx.activity.result.contract.ActivityResultContracts
import com.blazecode.eventtool.MainActivity
import com.blazecode.eventtool.R
import com.blazecode.eventtool.data.Event
import com.blazecode.eventtool.util.converter.LocalDateJsonTypeConverter
import com.blazecode.eventtool.util.converter.LocalTimeJsonTypeConverter
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.FileOutputStream
import java.lang.reflect.Type
import java.time.LocalDate
import java.time.LocalTime


class DataBaseExporter(val activity: MainActivity) {

    var json: String = ""

    fun exportJson(context: Context, eventList: List<Event>) {
        val gson = GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDate::class.java, LocalDateJsonTypeConverter().nullSafe())
            .registerTypeAdapter(LocalTime::class.java, LocalTimeJsonTypeConverter().nullSafe())
            .create()
        val gsonType: Type = object : TypeToken<List<Event>>() {}.type

        json = gson.toJson(eventList, gsonType)
        startIntent()
    }

    private fun startIntent(){
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
            putExtra(Intent.EXTRA_TITLE, "${activity.getString(R.string.FILE_NAME_EXPORT)}_${LocalDate.now()}.json")
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS))
        }
        resultLauncher.launch(intent)
    }

    private var resultLauncher = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            if(data != null) writeJsonToFile(Uri.parse(data.dataString))
        }
    }

    private fun writeJsonToFile(uri: Uri){
        try {
            activity.contentResolver.openFileDescriptor(uri, "w")?.use {
                FileOutputStream(it.fileDescriptor).use {
                    it.write((json).toByteArray())
                }
            }
            activity.sendToast(activity.getString(R.string.export_successful))
        } catch (e: java.lang.Exception) {
            activity.sendErrorDialog(e.toString().split("Exception: ")[2])
        }
    }
}