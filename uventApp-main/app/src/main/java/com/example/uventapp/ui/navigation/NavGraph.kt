package com.example.uventapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
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

        composable(Screen.DetailEvent.route + "/{eventId}") { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            DetailEventScreen(navController, eventId)
        }

        composable(
            route = Screen.RegistrationFormScreen.route,
            arguments = listOf(navArgument("eventName") { defaultValue = "" })
        ) { backStackEntry ->
            val eventName = backStackEntry.arguments?.getString("eventName") ?: ""
            RegistrationFormScreen(navController, eventName)
        }

        composable(Screen.EditRegistration.route + "/{eventName}") { backStackEntry ->
            val eventName = backStackEntry.arguments?.getString("eventName") ?: ""
            EditRegistrationScreen(navController, eventName)
        }

        composable(
            route = Screen.MyRegisteredEvent.route,
            arguments = listOf(navArgument("eventName") { defaultValue = "" })
        ) { backStackEntry ->
            val eventName = backStackEntry.arguments?.getString("eventName") ?: ""
            MyRegisteredEventScreen(eventName = eventName)
        }
    }
}
