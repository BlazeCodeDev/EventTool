/*
 *
 *  * Copyright (c) BlazeCode / Ralf Lehmann, 2022.
 *
 */

package com.blazecode.tsviewer.util.updater

import android.content.Context
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewModelScope
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.blazecode.eventtool.BuildConfig
import com.blazecode.eventtool.R
import com.blazecode.eventtool.database.DataBaseExporter
import com.blazecode.eventtool.database.EventRepository
import com.blazecode.scrapguidev2.util.LinkUtil
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@Composable
fun GitHubUpdater(context: Context, exporter : DataBaseExporter) {
    val release = remember{ mutableStateOf(GitHubRelease("","", arrayListOf())) }
    val scope = rememberCoroutineScope()

    val download = rememberSaveable{ mutableStateOf(false) }
    val doBackup = rememberSaveable{ mutableStateOf(false) }
    val showDialog = rememberSaveable{ mutableStateOf(false) }
    val showBackupDialog = rememberSaveable{ mutableStateOf(false) }
    val extendCard = rememberSaveable{ mutableStateOf(false) }

    fun parseJSON(input: String){
        val gson = GsonBuilder().create()
        val releases: Array<GitHubRelease> = gson.fromJson(input, Array<GitHubRelease>::class.java)
        val latestReleaseVersion = releases?.get(0)?.tag_name?.removePrefix("V")

        if(BuildConfig.VERSION_NAME != latestReleaseVersion){
            releases?.get(0)?.let { release.value = releases[0] }
            Log.i("UPDATER", "Update found")
        } else {
            Log.i("UPDATER", "No update found")
        }
    }

    fun checkForUpdate(){
        GlobalScope.launch {
            val queue = Volley.newRequestQueue(context)
            val url = context.resources.getString(R.string.GITHUB_RELEASES_URL)

            val stringRequest = StringRequest(
                Request.Method.GET, url,
                { response ->
                    parseJSON(response)
                },
                {
                })

            // Add the request to the RequestQueue.
            queue.add(stringRequest)
        }
    }

    LaunchedEffect(context){
        checkForUpdate()
    }

    if(release.value != GitHubRelease("","", arrayListOf())){
        showDialog.value = true
    }

    AnimatedVisibility(showDialog.value){
        var icon: Painter = painterResource(R.drawable.ic_expand_more)
        val showMoreIcon: Painter = painterResource(R.drawable.ic_expand_more)
        val showLessIcon: Painter = painterResource(R.drawable.ic_expand_less)
        Card (modifier = Modifier
            .fillMaxWidth()
            .padding(dimensionResource(R.dimen.medium_padding))){
            Column {
                Row (verticalAlignment = Alignment.CenterVertically){
                    Text(modifier = Modifier
                        .padding(dimensionResource(R.dimen.medium_padding))
                        .weight(2f),
                        text = stringResource(R.string.update_available, release.value.tag_name),
                        softWrap = true
                    )

                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimensionResource(R.dimen.medium_padding))
                        .weight(2f), contentAlignment = Alignment.CenterEnd){
                        Row {
                            Button(onClick = { showBackupDialog.value = true }) { Text(stringResource(R.string.download)) }
                            IconButton(onClick = {
                                extendCard.value = !extendCard.value
                                if(extendCard.value) icon = showLessIcon
                                else icon = showMoreIcon
                            }) { Icon(icon, "extend") }
                        }
                    }
                }
                AnimatedVisibility(extendCard.value){
                    Text(modifier = Modifier.padding(dimensionResource(R.dimen.medium_padding)), text = release.value.body)
                }
            }
        }
    }

    if(showBackupDialog.value) {
        AlertDialog(onDismissRequest = { showBackupDialog.value = false},
            title = { Text(stringResource(R.string.backup_database_title)) },
            text = { Text(stringResource(R.string.backup_database_message)) },
            confirmButton = {
                FilledTonalButton(onClick = { doBackup.value = true }) {
                    Text(text = stringResource(R.string.backup_database_button))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showBackupDialog.value = false; download.value = true }) {
                    Text(text = stringResource(R.string.download_update))
                }
            })
    }

    LaunchedEffect(doBackup.value){
        if(doBackup.value) {
            scope.launch(Dispatchers.IO) {
                export(scope, exporter, context)
            }.join()
        }
    }

    if(download.value) {
        download.value = false
        LinkUtil.Builder(context).link(release.value.assets[0].browser_download_url).open()
    }
}

private fun export(scope: CoroutineScope, exporter: DataBaseExporter, context: Context){
    val repository = EventRepository()

    scope.launch(Dispatchers.IO) {
        val list = repository.getEventList(context)
        exporter.exportJson(context, list)
    }
}