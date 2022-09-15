/*
 *
 *  * Copyright (c) BlazeCode / Ralf Lehmann, 2022.
 *
 */

package com.blazecode.eventtool.util

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat.checkSelfPermission
import com.blazecode.eventtool.MainActivity

class PermissionManager(val activity: MainActivity) {

    fun askForNotificationPermission() {
        requestPermissions(activity, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
    }

    fun checkNotificationPermission(): Boolean {
        return checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    }

    fun askForAlarmPermission() {
        requestPermissions(activity, arrayOf(Manifest.permission.SCHEDULE_EXACT_ALARM), 2)
    }

    fun checkAlarmPermission(): Boolean {
        return checkSelfPermission(activity, Manifest.permission.SCHEDULE_EXACT_ALARM) == PackageManager.PERMISSION_GRANTED
    }
}