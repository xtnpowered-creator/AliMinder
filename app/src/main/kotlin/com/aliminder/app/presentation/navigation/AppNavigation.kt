package com.aliminder.app.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aliminder.app.presentation.screens.all.AllScreen
import com.aliminder.app.presentation.screens.all.EventsScreen
import com.aliminder.app.presentation.screens.all.PendingScreen
import com.aliminder.app.presentation.screens.all.TasksScreen
import com.aliminder.app.presentation.screens.settings.SettingsScreen

/**
 * Navigation routes for AliMinder app
 */
sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object All : Screen("all", "Duties", Icons.Default.Dashboard)
    object Events : Screen("events", "Events", Icons.Default.Schedule)
    object Tasks : Screen("tasks", "Tasks", Icons.Default.TaskAlt)
    object Pending : Screen("pending", "Pending", Icons.Outlined.HelpOutline)
    object Settings : Screen("settings", "Settings", Icons.Outlined.Settings)
}

val bottomNavItems = listOf(
    Screen.All,
    Screen.Events,
    Screen.Tasks,
    Screen.Pending,
    Screen.Settings
)

/**
 * Main navigation scaffold with bottom navigation bar
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = {
            AliMinderBottomBar(navController = navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.All.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.All.route) { AllScreen() }
            composable(Screen.Events.route) { EventsScreen() }
            composable(Screen.Tasks.route) { TasksScreen() }
            composable(Screen.Pending.route) { PendingScreen() }
            composable(Screen.Settings.route) { SettingsScreen() }
        }
    }
}
