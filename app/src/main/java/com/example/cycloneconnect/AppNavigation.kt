package com.example.cycloneconnect

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cycloneconnect.ui.screens.HomeScreen
import com.example.cycloneconnect.ui.screens.ComingSoonScreen
import com.example.cycloneconnect.ui.screens.ProfileScreen
import com.example.cycloneconnect.ui.screens.LoginScreen

// Stores the names of all app destinations.
object AppRoutes {
    const val HOME = "home"
    const val EMERGENCY = "emergency"
    const val FAMILY = "family"
    const val SEARCH = "search/{city}"
    fun searchRoute(city: String) = "search/${android.net.Uri.encode(city)}"
    const val NOTIFICATIONS = "notifications"
    const val VOLUNTEER = "volunteer"
    const val DONATE = "donate"
    const val PRIVACY = "privacy"
    const val HELP = "help"
    const val SETTINGS = "settings"
    const val ABOUT = "about"
    const val PROFILE = "profile"
    const val LOGIN = "login"
    const val SOS = "sos"
    const val OFFICIAL_ALERTS = "official_alerts"
    const val REGIONAL_NEWS = "regional_news"

}

// Controls navigation between app screens.
@Composable
fun AppNavigation(
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppRoutes.HOME
    ) {
        // Home screen
        composable(AppRoutes.HOME) {
            HomeScreen(
                onNavigate = { route ->
                    navController.navigate(route)
                },
                isDarkTheme = isDarkTheme,
                onThemeChange = onThemeChange
            )
        }

        // Emergency-related screens
        composable(AppRoutes.EMERGENCY) {
            ComingSoonScreen("Emergency Information") {
                navController.popBackStack()
            }
        }

        composable(AppRoutes.FAMILY) {
            ComingSoonScreen("Family & Friends") {
                navController.popBackStack()
            }
        }

        composable(AppRoutes.SOS) {
            ComingSoonScreen("Emergency SOS") {
                navController.popBackStack()
            }
        }

        // Search screen
        composable(AppRoutes.SEARCH) { backStackEntry ->
            val city = backStackEntry.arguments?.getString("city") ?: "Search Locations"
            ComingSoonScreen(city) {
                navController.popBackStack()
            }
        }

        // News and notification screens
        composable(AppRoutes.NOTIFICATIONS) {
            ComingSoonScreen("Notifications") {
                navController.popBackStack()
            }
        }

        composable(AppRoutes.OFFICIAL_ALERTS) {
            ComingSoonScreen("Official Cyclone Alerts") {
                navController.popBackStack()
            }
        }

        composable(AppRoutes.REGIONAL_NEWS) {
            ComingSoonScreen("Regional News") {
                navController.popBackStack()
            }
        }

        // Other app screens
        composable(AppRoutes.VOLUNTEER) {
            ComingSoonScreen("Volunteer") {
                navController.popBackStack()
            }
        }

        composable(AppRoutes.DONATE) {
            ComingSoonScreen("Donate") {
                navController.popBackStack()
            }
        }

        composable(AppRoutes.PRIVACY) {
            ComingSoonScreen("Privacy") {
                navController.popBackStack()
            }
        }

        composable(AppRoutes.HELP) {
            ComingSoonScreen("Help & Support") {
                navController.popBackStack()
            }
        }

        composable(AppRoutes.SETTINGS) {
            ComingSoonScreen("Settings") {
                navController.popBackStack()
            }
        }

        composable(AppRoutes.ABOUT) {
            ComingSoonScreen("About CycloneConnect") {
                navController.popBackStack()
            }
        }

        composable(AppRoutes.PROFILE) {
            ProfileScreen(
                onLoginClick = {
                    navController.navigate(AppRoutes.LOGIN)
                },
                onRegisterClick = {

                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(AppRoutes.LOGIN) {
            LoginScreen(
                onLoginClick = { email, password ->

                },
                onRegisterClick = {

                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

    }
}
