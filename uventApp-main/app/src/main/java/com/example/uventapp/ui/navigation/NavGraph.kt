package com.example.uventapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.uventapp.ui.screen.auth.LoginScreen
import com.example.uventapp.ui.screen.auth.RegistrationScreen
import com.example.uventapp.ui.screen.auth.SplashScreen
import com.example.uventapp.ui.screen.registration.EditRegistrationScreen
import com.example.uventapp.ui.screen.registration.MyRegisteredEventScreen
import com.example.uventapp.ui.screen.registration.RegistrationFormScreen
import com.example.uventapp.ui.screen.home.HomeScreen
import com.example.uventapp.ui.screen.event.DetailEventScreen
import com.example.uventapp.ui.screen.event.EventListScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Splash.route) {

        composable(Screen.Splash.route) { SplashScreen(navController) }
        composable(Screen.Login.route) { LoginScreen(navController) }
        composable(Screen.Register.route) { RegistrationScreen(navController) }
        composable(Screen.Home.route) { HomeScreen(navController) }
        composable(Screen.EventList.route) { EventListScreen(navController) }

        // --- PERBAIKAN 1 ---
        // Pola yang salah: Screen.DetailEvent.route + "/{eventId}"
        // Pola yang benar:
        composable(
            route = Screen.DetailEvent.route, // Hanya rute dari Screen.kt
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            DetailEventScreen(navController, eventId)
        }
        // -------------------

        composable(
            route = Screen.RegistrationFormScreen.route,
            arguments = listOf(navArgument("eventName") { defaultValue = "" })
        ) { backStackEntry ->
            val eventName = backStackEntry.arguments?.getString("eventName") ?: ""
            RegistrationFormScreen(navController, eventName)
        }

        // --- PERBAIKAN 2 (INI YANG MENYEBABKAN CRASH ANDA) ---
        // Pola yang salah: Screen.EditRegistration.route + "/{eventName}"
        // Pola yang benar:
        composable(
            route = Screen.EditRegistration.route, // Hanya rute dari Screen.kt
            arguments = listOf(navArgument("eventName") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventName = backStackEntry.arguments?.getString("eventName") ?: ""
            EditRegistrationScreen(navController, eventName)
        }
        // -------------------

        composable(
            route = Screen.MyRegisteredEvent.route,
            arguments = listOf(navArgument("eventName") {
                type = NavType.StringType
                nullable = true
                defaultValue = ""
            })
        ) { backStackEntry ->
            val eventName = backStackEntry.arguments?.getString("eventName") ?: ""
            MyRegisteredEventScreen(navController = navController, eventName = eventName)
        }
    }
}