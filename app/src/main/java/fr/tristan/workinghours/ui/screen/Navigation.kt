package fr.tristan.workinghours.ui.screen

import android.util.Log
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import fr.tristan.workinghours.ui.screen.home.DayViewModel
import fr.tristan.workinghours.ui.screen.home.HomeScreen
import fr.tristan.workinghours.ui.screen.settings.SettingsScreen

@Composable
fun AppNavigation(modifier: Modifier = Modifier, dayViewModel: DayViewModel) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = AppScreen.Home.name,
        modifier = modifier
    ) {
        composable(
            route = AppScreen.Home.name,
            popEnterTransition = { slideInHorizontally(animationSpec = tween(500), initialOffsetX = { -it }) },
            exitTransition = { slideOutHorizontally(animationSpec = tween(500), targetOffsetX = { -it }) },
        ) {
            HomeScreen(
                dayViewModel,
                onSettingsClick = {
                    dayViewModel.setUiSettingsTime()
                    navController.navigate(AppScreen.Settings.name)
                }
            )
        }

        composable(
            route = AppScreen.Settings.name,
            popExitTransition = { slideOutHorizontally(animationSpec = tween(500), targetOffsetX = { it }) },
            enterTransition = { slideInHorizontally(animationSpec = tween(500), initialOffsetX = { it }) },
        ) {
            SettingsScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                dayViewModel = dayViewModel,
                onSettingsTimeConfirm = {
                    dayViewModel.updateSettingsTimePrev()
                }
            )
        }
    }
}

enum class AppScreen {
    Home,
    Settings,
}