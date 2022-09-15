/*
 *
 *  * Copyright (c) BlazeCode / Ralf Lehmann, 2022.
 *
 */

package com.blazecode.eventtool.navigation

sealed class NavRoutes(val route: String) {
    object Home: NavRoutes("home")
    object NewEvent: NavRoutes("newEvent")
    object Settings: NavRoutes("settings")
    object OpenSourceLicenses: NavRoutes("openSourceLicenses")
}