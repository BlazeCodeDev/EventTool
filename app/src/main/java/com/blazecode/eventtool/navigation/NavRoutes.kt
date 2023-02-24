/*
 *
 *  * Copyright (c) BlazeCode / Ralf Lehmann, 2023.
 *
 */

package com.blazecode.eventtool.navigation

sealed class NavRoutes(val route: String) {
    object Home: NavRoutes("home")
    object NewEvent: NavRoutes("newEvent")
    object Search: NavRoutes("search")
    object Settings: NavRoutes("settings")
    object OpenSourceLicenses: NavRoutes("openSourceLicenses")
}