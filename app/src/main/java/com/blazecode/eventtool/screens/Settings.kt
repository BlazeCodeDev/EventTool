/*
 *
 *  * Copyright (c) BlazeCode / Ralf Lehmann, 2022.
 *
 */

package com.blazecode.eventtool.screens

import android.Manifest.permission.POST_NOTIFICATIONS
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.blazecode.eventtool.BuildConfig
import com.blazecode.eventtool.R
import com.blazecode.eventtool.database.DataBaseExporter
import com.blazecode.eventtool.database.DataBaseImporter
import com.blazecode.eventtool.navigation.NavRoutes
import com.blazecode.eventtool.ui.theme.EventToolTheme
import com.blazecode.eventtool.ui.theme.Typography
import com.blazecode.eventtool.viewmodels.SettingsViewModel
import com.blazecode.eventtool.views.DefaultPreference
import com.blazecode.eventtool.views.PreferenceGroup
import com.blazecode.eventtool.views.SwitchPreference
import com.blazecode.scrapguidev2.util.LinkUtil
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings(viewModel: SettingsViewModel = viewModel(), navController: NavController, exporter: DataBaseExporter, importer: DataBaseImporter) {

    EventToolTheme {
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
        Scaffold(
            topBar = { TopAppBar(viewModel, LocalContext.current, scrollBehavior, navController) },
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            content = { paddingValues ->
                Column(modifier = Modifier
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())) {
                    MainLayout(viewModel, navController, exporter, importer)
                }
            },
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun MainLayout(viewModel: SettingsViewModel, navController: NavController, exporter: DataBaseExporter, importer: DataBaseImporter){
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val showImportDialog = rememberSaveable{ mutableStateOf(false) }
    val showNotificationPermissionNeeded = rememberSaveable{ mutableStateOf(false) }
    val showScheduleAlarmsPermissionNeeded = rememberSaveable{ mutableStateOf(false) }
    val hasNotificationPermission = rememberPermissionState(POST_NOTIFICATIONS)

    val remindersEnabled = rememberSaveable{ mutableStateOf(false) }
    val colorfulCalendarDays = rememberSaveable{ mutableStateOf(false) }

    LaunchedEffect(context){
        this.launch {
            remindersEnabled.value = viewModel.getRemindersEnabled(context)
            colorfulCalendarDays.value = viewModel.getColorfulDays(context)
        }
    }

    Column {
        // GENERAL
        PreferenceGroup(stringResource(R.string.general)){
            SwitchPreference(
                painterResource(R.drawable.ic_reminder),
                stringResource(R.string.event_reminders),
                stringResource(R.string.event_reminders_summary,
                    integerResource(R.integer.REMINDER_AMOUNT_HOURS_BEFORE_TIME_READY)) , remindersEnabled.value) { checked ->


                if(checked){
                    if(hasNotificationPermission.status.isGranted && viewModel.canScheduleExactAlarms()){
                        remindersEnabled.value = true
                        scope.launch { viewModel.setRemindersEnabled(context, true) }
                    }

                    if(hasNotificationPermission.status.shouldShowRationale){
                        remindersEnabled.value = false
                        showNotificationPermissionNeeded.value = true
                    } else if (!hasNotificationPermission.status.isGranted){
                        remindersEnabled.value = false
                        hasNotificationPermission.launchPermissionRequest()
                    }

                    if(!viewModel.canScheduleExactAlarms()){
                        remindersEnabled.value = false
                        showScheduleAlarmsPermissionNeeded.value = true
                    }
                } else {
                    remindersEnabled.value = false
                    scope.launch { viewModel.setRemindersEnabled(context, false) }
                }
            }
        }

        // APPEARANCE
        PreferenceGroup(stringResource(R.string.appearance)){
            SwitchPreference(painterResource(R.drawable.ic_colorful), stringResource(R.string.colorful_calendar_days), null, colorfulCalendarDays.value) { checked ->
                colorfulCalendarDays.value = checked
                scope.launch { viewModel.setColorfulDays(context, checked) }
            }
        }

        // DATA
        PreferenceGroup(stringResource(R.string.data)){
            DefaultPreference(painterResource(R.drawable.ic_export), stringResource(R.string.export_data), null) { viewModel.exportDatabase(exporter) }
            DefaultPreference(painterResource(R.drawable.ic_import), stringResource(R.string.import_data), null) { showImportDialog.value = true }
        }

        // ABOUT
        PreferenceGroup(stringResource(R.string.about)){
            val link = stringResource(R.string.GITHUB_SOURCE_URL)
            DefaultPreference(painterResource(R.drawable.ic_open_source_licenses), stringResource(R.string.open_source_licenses), null) { navController.navigate(NavRoutes.OpenSourceLicenses.route) }
            DefaultPreference(painterResource(R.drawable.ic_github), stringResource(R.string.source_code), null) {
                LinkUtil.Builder(context).link(link).open()}
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()){
                Text( text = ("${stringResource(R.string.app_version)}: ${BuildConfig.VERSION_NAME}"),
                    modifier = Modifier.padding(dimensionResource(R.dimen.medium_padding)),
                    style = Typography.bodySmall)
            }
        }
    }

    // NOTIFIACTION PERMISSION NEEDED
    if(showNotificationPermissionNeeded.value){
        AlertDialog(
            onDismissRequest = {showNotificationPermissionNeeded.value = false},
            title = { Text(stringResource(R.string.noificationDialog_permission_needed)) },
            text = { Text(stringResource(R.string.noificationDialog_permission_needed_message)) },
            dismissButton = { OutlinedButton(onClick = { showNotificationPermissionNeeded.value = false }) { Text(stringResource(R.string.cancel)) } },
            confirmButton = { Button(onClick = { showNotificationPermissionNeeded.value = false; viewModel.openNotificationSettings() }) { Text(stringResource(R.string.settings)) } }
        )
    }

    // SCHEDULE ALARMS PERMISSION NEEDED
    if(showScheduleAlarmsPermissionNeeded.value){
        AlertDialog(
            onDismissRequest = {showScheduleAlarmsPermissionNeeded.value = false},
            title = { Text(stringResource(R.string.schedule_exact_alarms_title)) },
            text = { Text(stringResource(R.string.schedule_exact_alarms_message)) },
            dismissButton = { OutlinedButton(onClick = { showScheduleAlarmsPermissionNeeded.value = false }) { Text(stringResource(R.string.cancel)) } },
            confirmButton = { Button(onClick = { showScheduleAlarmsPermissionNeeded.value = false; viewModel.openExactAlarmSettings() }) { Text(stringResource(R.string.settings)) } }
        )
    }

    // IMPORT DIALOG
    if(showImportDialog.value){
        AlertDialog(
            onDismissRequest = {showImportDialog.value = false},
            title = { Text(stringResource(R.string.import_data)) },
            text = { Text(stringResource(R.string.import_dialog_message)) },
            dismissButton = { OutlinedButton(onClick = { showImportDialog.value = false; viewModel.importDatabase(importer) }) { Text(stringResource(R.string.confirm)) } },
            confirmButton = { Button(onClick = { showImportDialog.value = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopAppBar(viewModel: SettingsViewModel, context: Context, scrollBehavior: TopAppBarScrollBehavior, navController: NavController){
    LargeTopAppBar(
        title = { Text(text = stringResource(R.string.settings)) },
        navigationIcon = {
            Box (modifier = Modifier
                .size(dimensionResource(R.dimen.icon_button_size))
                .clickable { navController.popBackStack() },
                contentAlignment = Alignment.Center){
                Icon(painterResource(R.drawable.ic_back), "back")
            }
        },
        scrollBehavior = scrollBehavior
    )
}