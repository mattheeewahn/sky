package com.skytrace.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.skytrace.app.ui.screens.asteroidcheck.AsteroidCheckScreen
import com.skytrace.app.ui.screens.blink.BlinkScreen
import com.skytrace.app.ui.screens.collection.CollectionScreen
import com.skytrace.app.ui.screens.home.HomeScreen
import com.skytrace.app.ui.screens.observation.ObservationLogScreen
import com.skytrace.app.ui.screens.observation.AddObservationScreen
import com.skytrace.app.ui.screens.search.SearchScreen
import com.skytrace.app.ui.screens.settings.SettingsScreen
import com.skytrace.app.ui.screens.skymap.SkyMapScreen
import com.skytrace.app.ui.screens.telescope.TelescopePointingScreen

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object SkyMap : Screen("skymap")
    data object Search : Screen("search")
    data object ObservationLog : Screen("observations")
    data object AddObservation : Screen("observations/add?objectName={objectName}&objectType={objectType}")
    data object Collection : Screen("collection")
    data object AsteroidCheck : Screen("asteroid_check")
    data object Blink : Screen("blink")
    data object TelescopePointing : Screen("telescope/{objectId}")
    data object Settings : Screen("settings")
}

@Composable
fun SkyTraceNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(Screen.SkyMap.route) {
            SkyMapScreen(navController = navController)
        }
        composable(Screen.Search.route) {
            SearchScreen(navController = navController)
        }
        composable(Screen.ObservationLog.route) {
            ObservationLogScreen(navController = navController)
        }
        composable(
            route = "observations/add?objectName={objectName}&objectType={objectType}",
            arguments = listOf(
                navArgument("objectName") { type = NavType.StringType; defaultValue = "" },
                navArgument("objectType") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            AddObservationScreen(
                navController = navController,
                objectName = backStackEntry.arguments?.getString("objectName") ?: "",
                objectType = backStackEntry.arguments?.getString("objectType") ?: ""
            )
        }
        composable(Screen.Collection.route) {
            CollectionScreen(navController = navController)
        }
        composable(Screen.AsteroidCheck.route) {
            AsteroidCheckScreen(navController = navController)
        }
        composable(Screen.Blink.route) {
            BlinkScreen(navController = navController)
        }
        composable(
            route = "telescope/{objectId}",
            arguments = listOf(navArgument("objectId") { type = NavType.StringType })
        ) { backStackEntry ->
            TelescopePointingScreen(
                navController = navController,
                objectId = backStackEntry.arguments?.getString("objectId") ?: ""
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }
    }
}
