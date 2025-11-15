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
import com.example.uventapp.ui.screen.registration.EditRegistrationScreen
import com.example.uventapp.ui.screen.registration.MyRegisteredEventScreen
import com.example.uventapp.ui.screen.registration.RegistrationFormScreen
import com.example.uventapp.ui.screen.home.HomeScreen
import com.example.uventapp.ui.screen.event.DetailEventScreen
import com.example.uventapp.ui.screen.event.EventListScreen
import com.example.uventapp.ui.screen.feedback.AddFeedbackScreen
import com.example.uventapp.ui.screen.feedback.AllFeedbackScreen
// --- Import untuk Dokumentasi ---
import com.example.uventapp.ui.screen.documentation.AddDocumentationScreen
import com.example.uventapp.ui.screen.documentation.AllDocumentationScreen
// --- IMPORT BARU UNTUK PROFIL ---
import com.example.uventapp.ui.screen.profile.ProfileScreen
import com.example.uventapp.ui.screen.profile.ProfileViewModel

@Composable
fun NavGraph(navController: NavHostController) {

    // --- PERBAIKAN DI SINI ---
    // Daftarkan kedua ViewModel di sini
    val eventViewModel: EventManagementViewModel = viewModel()
    val profileViewModel: ProfileViewModel = viewModel() // <-- 1. Buat ViewModel di sini

    NavHost(navController = navController, startDestination = Screen.Splash.route) {

        composable(Screen.Splash.route) { SplashScreen(navController) }

        // --- PERBAIKAN DI SINI ---
        // 2. Kirimkan ViewModel ke layar-layar yang membutuhkan
        composable(Screen.Login.route) {
            LoginScreen(navController, profileViewModel)
        }
        composable(Screen.Register.route) {
            RegistrationScreen(navController, profileViewModel)
        }
        // ----------------------------------------

        composable(Screen.Home.route) { HomeScreen(navController) }

        composable(Screen.EventList.route) {
            EventListScreen(navController = navController, viewModel = eventViewModel)
        }

        // ... (sisa composable lainnya tetap sama) ...

        composable(
            route = Screen.DetailEvent.route,
            arguments = listOf(navArgument("eventId") { type = NavType.IntType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getInt("eventId")
            DetailEventScreen(navController = navController, eventId = eventId, viewModel = eventViewModel)
        }

        composable(
            route = Screen.RegistrationFormScreen.route,
            arguments = listOf(navArgument("eventId") { type = NavType.IntType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getInt("eventId")
            RegistrationFormScreen(
                navController = navController,
                viewModel = eventViewModel,
                eventId = eventId
            )
        }

        composable(
            route = Screen.EditRegistration.route,
            arguments = listOf(navArgument("eventId") { type = NavType.IntType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getInt("eventId")
            EditRegistrationScreen(
                navController = navController,
                viewModel = eventViewModel,
                eventId = eventId
            )
        }

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
                viewModel = eventViewModel
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

        composable(
            route = Screen.AddFeedback.route,
            arguments = listOf(navArgument("eventId") { type = NavType.IntType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getInt("eventId")
            AddFeedbackScreen(
                navController = navController,
                viewModel = eventViewModel,
                eventId = eventId
            )
        }

        composable(
            route = Screen.AllFeedback.route,
            arguments = listOf(navArgument("eventId") { type = NavType.IntType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getInt("eventId")
            AllFeedbackScreen(
                navController = navController,
                viewModel = eventViewModel,
                eventId = eventId
            )
        }

        composable(
            route = Screen.AllDocumentation.route,
            arguments = listOf(navArgument("eventId") { type = NavType.IntType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getInt("eventId")
            AllDocumentationScreen(
                navController = navController,
                viewModel = eventViewModel,
                eventId = eventId
            )
        }

        composable(
            route = Screen.AddDocumentation.route,
            arguments = listOf(
                navArgument("eventId") { type = NavType.IntType },
                navArgument("docId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getInt("eventId")
            val docIdString = backStackEntry.arguments?.getString("docId")
            val docId = docIdString?.toIntOrNull()

            AddDocumentationScreen(
                navController = navController,
                viewModel = eventViewModel,
                eventId = eventId,
                docId = docId
            )
        }

        // --- PERBAIKAN DI SINI ---
        // 3. Kirimkan juga ViewModel ke ProfileScreen
        composable(Screen.Profile.route) {
            ProfileScreen(
                navController = navController,
                profileViewModel = profileViewModel
            )
        }
    }
}
