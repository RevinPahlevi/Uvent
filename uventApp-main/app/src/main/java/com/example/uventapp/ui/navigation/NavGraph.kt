package com.example.uventapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.uventapp.ui.screen.auth.LoginScreen
import com.example.uventapp.ui.screen.auth.RegistrationScreen
import com.example.uventapp.ui.screen.auth.SplashScreen
import com.example.uventapp.ui.screen.event.AddEventScreen
import com.example.uventapp.ui.screen.event.EditEventScreen
import com.example.uventapp.ui.screen.event.EventManagementViewModel
// Hapus MyEventsScreen karena sudah digabung
// import com.example.uventapp.ui.screen.event.MyEventsScreen
import com.example.uventapp.ui.screen.registration.EditRegistrationScreen
import com.example.uventapp.ui.screen.registration.MyRegisteredEventScreen
import com.example.uventapp.ui.screen.registration.RegistrationFormScreen
import com.example.uventapp.ui.screen.home.HomeScreen
import com.example.uventapp.ui.screen.event.DetailEventScreen
import com.example.uventapp.ui.screen.event.EventListScreen

@Composable
fun NavGraph(navController: NavHostController) {

    // Inisialisasi ViewModel di sini agar bisa dipakai di beberapa layar
    val eventViewModel: EventManagementViewModel = viewModel()

    NavHost(navController = navController, startDestination = Screen.Splash.route) {

        composable(Screen.Splash.route) { SplashScreen(navController) }
        composable(Screen.Login.route) { LoginScreen(navController) }
        composable(Screen.Register.route) { RegistrationScreen(navController) }
        composable(Screen.Home.route) { HomeScreen(navController) }

        composable(Screen.EventList.route) {
            EventListScreen(navController = navController, viewModel = eventViewModel)
        }

        composable(
            route = Screen.DetailEvent.route,
            arguments = listOf(navArgument("eventId") { type = NavType.IntType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getInt("eventId")
            DetailEventScreen(navController = navController, eventId = eventId, viewModel = eventViewModel)
        }

        composable(
            route = Screen.RegistrationFormScreen.route,
            arguments = listOf(navArgument("eventId") { type = NavType.IntType }) // Gunakan Int
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getInt("eventId")
            RegistrationFormScreen(
                navController = navController,
                viewModel = eventViewModel, // Kirim ViewModel
                eventId = eventId
            )
        }

        // --- PERBAIKAN: Rute EditRegistration ---
        composable(
            route = Screen.EditRegistration.route,
            // Ambil eventId (Int) dari rute
            arguments = listOf(navArgument("eventId") { type = NavType.IntType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getInt("eventId")
            EditRegistrationScreen(
                navController = navController,
                viewModel = eventViewModel, // Kirim ViewModel
                eventId = eventId
            )
        }
        // ----------------------------------------

        composable(
            route = Screen.MyRegisteredEvent.route,
            arguments = listOf(navArgument("eventName") {
                type = NavType.StringType
                nullable = true
                defaultValue = ""
            })
        ) { backStackEntry ->
            val eventName = backStackEntry.arguments?.getString("eventName") ?: ""
            MyRegisteredEventScreen(
                navController = navController,
                eventName = eventName,
                viewModel = eventViewModel // Kirim ViewModel
            )
        }

        composable(Screen.AddEvent.route) {
            AddEventScreen(navController = navController, viewModel = eventViewModel)
        }

        composable(
            route = Screen.EditEvent.route,
            arguments = listOf(navArgument("eventId") { type = NavType.IntType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getInt("eventId")
            EditEventScreen(navController = navController, viewModel = eventViewModel, eventId = eventId)
        }
    }
}