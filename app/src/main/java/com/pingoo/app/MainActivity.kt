package com.pingoo.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pingoo.app.ui.navigation.Screen
import com.pingoo.app.ui.screens.DestinationsScreen
import com.pingoo.app.ui.screens.HistoryScreen
import com.pingoo.app.ui.screens.HomeScreen
import com.pingoo.app.ui.screens.JourneyScreen
import com.pingoo.app.ui.screens.SettingsScreen
import com.pingoo.app.ui.theme.PingooTheme
import com.pingoo.app.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val userSettings by viewModel.userSettings.collectAsState()
            val strings by viewModel.strings.collectAsState()
            val destinations by viewModel.destinations.collectAsState()
            val activeDestinations by viewModel.activeDestinations.collectAsState()

            PingooTheme(darkTheme = userSettings.darkTheme) {
                MainAppScreen(
                    strings = strings,
                    userSettings = userSettings,
                    destinations = destinations,
                    activeDestinations = activeDestinations,
                    viewModel = viewModel
                )
            }
        }
    }
}

data class BottomNavItem(
    val screen: Screen,
    val title: String,
    val icon: ImageVector
)

@Composable
fun MainAppScreen(
    strings: com.pingoo.app.ui.i18n.AppStrings,
    userSettings: com.pingoo.app.domain.model.UserSettings,
    destinations: List<com.pingoo.app.domain.model.Destination>,
    activeDestinations: List<com.pingoo.app.domain.model.Destination>,
    viewModel: MainViewModel
) {
    val navController = rememberNavController()
    val navItems = listOf(
        BottomNavItem(Screen.Home, strings.navHome, Icons.Default.Home),
        BottomNavItem(Screen.Destinations, strings.navDestinations, Icons.Default.Place),
        BottomNavItem(Screen.Journey, strings.navJourney, Icons.Default.Navigation),
        BottomNavItem(Screen.History, strings.navHistory, Icons.Default.History),
        BottomNavItem(Screen.Settings, strings.navSettings, Icons.Default.Settings)
    )

    val selectedDestination by viewModel.selectedDestination.collectAsState()
    val stagedStops by viewModel.stagedStops.collectAsState()
    val multiStopEvaluation by viewModel.multiStopEvaluation.collectAsState()
    val journeyHistory by viewModel.journeyHistory.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                navItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = currentRoute == item.screen.route,
                        onClick = {
                            if (currentRoute != item.screen.route) {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    strings = strings,
                    activeDestinations = activeDestinations,
                    onNavigateToDestinations = {
                        navController.navigate(Screen.Destinations.route)
                    },
                    onToggleDestination = { viewModel.toggleDestinationActive(it) }
                )
            }
            composable(Screen.Destinations.route) {
                DestinationsScreen(
                    strings = strings,
                    destinations = destinations,
                    language = userSettings.language,
                    onAddDestination = { name, lat, lng, radius, callback ->
                        viewModel.addDestination(name, lat, lng, radius, callback)
                    },
                    onDeleteDestination = { viewModel.deleteDestination(it) },
                    onToggleDestination = { viewModel.toggleDestinationActive(it) }
                )
            }
            composable(Screen.Journey.route) {
                JourneyScreen(
                    strings = strings,
                    destinations = destinations,
                    selectedDestination = selectedDestination,
                    stagedStops = stagedStops,
                    evaluation = multiStopEvaluation,
                    language = userSettings.language,
                    onSelectDestination = { viewModel.selectDestination(it) },
                    onAddStagedStop = { viewModel.addStagedStop(it) },
                    onRemoveStagedStop = { viewModel.removeStagedStop(it) },
                    onMoveStagedStopUp = { viewModel.moveStagedStopUp(it) },
                    onMoveStagedStopDown = { viewModel.moveStagedStopDown(it) },
                    onStartMultiStopJourney = { viewModel.startStagedMultiStopJourney(it) },
                    onPauseJourney = { viewModel.pauseJourney() },
                    onResumeJourney = { viewModel.resumeJourney() },
                    onDismissAlarmAndAdvance = { viewModel.dismissAlarmAndAdvance() },
                    onStopJourney = { viewModel.stopJourney() }
                )
            }
            composable(Screen.History.route) {
                HistoryScreen(
                    strings = strings,
                    historyItems = journeyHistory,
                    onDeleteHistoryItem = { viewModel.deleteHistoryItem(it) },
                    onClearAllHistory = { viewModel.clearAllHistory() }
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    strings = strings,
                    userSettings = userSettings,
                    onLanguageSelected = { viewModel.changeLanguage(it) },
                    onVibrationToggled = { viewModel.toggleVibration(it) },
                    onSnoozeDurationSelected = { viewModel.setSnoozeDuration(it) },
                    onClearAllData = { viewModel.clearAllHistory() }
                )
            }
        }
    }
}
