/*
 *
 *  * Copyright (c) BlazeCode / Ralf Lehmann, 2022.
 *
 */

package com.blazecode.eventtool

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material3.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.lifecycleScope
import com.blazecode.eventtool.data.Event
import com.blazecode.eventtool.database.DataBaseExporter
import com.blazecode.eventtool.database.DataBaseImporter
import com.blazecode.eventtool.navigation.NavRoutes
import com.blazecode.eventtool.screens.*
import com.blazecode.eventtool.ui.theme.EventToolTheme
import com.blazecode.eventtool.util.PermissionManager
import com.blazecode.eventtool.util.pdf.PdfPrinter
import com.blazecode.eventtool.viewmodels.HomeViewModel
import com.blazecode.eventtool.viewmodels.NewEventViewModel
import com.blazecode.eventtool.viewmodels.SettingsViewModel
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import kotlinx.coroutines.launch

var errorDialogMessage = mutableStateOf("")
var notificationTapEvent = mutableStateOf<Event?>(null)

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // HACKY WAY TO PREVENT WHITE SCREEN BETWEEN TRANSITIONS
        lifecycleScope.launch {
            window.setBackgroundDrawableResource(android.R.color.transparent)
        }

        // GET EVENT IF TAPPED NOTIFICATION
        notificationTapEvent.value = intent.extras?.getParcelable("event", Event::class.java)

        val permissionManager = PermissionManager(this)

        val exporter = DataBaseExporter(this)
        val importer = DataBaseImporter(this)
        val printer = PdfPrinter(this)
        // CONTENT
        setContent {
            val navController = rememberAnimatedNavController()
            AnimatedNavHost(navController = navController, startDestination = NavRoutes.Home.route) {
                // HOME
                composable(route= NavRoutes.Home.route,) { Home(HomeViewModel(application), navController, printer) }
                // NEW EVENT
                composable(NavRoutes.NewEvent.route) {
                    val event = navController.previousBackStackEntry?.savedStateHandle?.get<Event>("event")
                    event?.let {
                        NewEvent(NewEventViewModel(application, event), navController) }
                }
                // SETTINGS
                composable(NavRoutes.Settings.route){ Settings(SettingsViewModel(application), navController, permissionManager, exporter, importer) }
                // OPEN SOURCE LICENSES
                composable(NavRoutes.OpenSourceLicenses.route){ OpenSourceLicenses(navController) }
            }

            // ERROR DIALOG
            EventToolTheme {
                if(errorDialogMessage.value != ""){
                    AlertDialog(
                        onDismissRequest = { errorDialogMessage.value = "" },
                        title = { Text(stringResource(R.string.error)) },
                        text = { Text(errorDialogMessage.value) },
                        confirmButton = { OutlinedButton(onClick = { errorDialogMessage.value = "" })
                        { Text(stringResource(R.string.close)) } }
                    )
                }
            }
        }
    }

    fun sendToast(text:String){
        Toast.makeText(this, text, Toast.LENGTH_LONG).show()
        println(text)
    }

    fun sendErrorDialog(text: String){
        errorDialogMessage.value = text
    }
}

