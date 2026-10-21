package com.pingoo.app.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Destinations : Screen("destinations")
    data object Journey : Screen("journey")
    data object History : Screen("history")
    data object Settings : Screen("settings")
}
