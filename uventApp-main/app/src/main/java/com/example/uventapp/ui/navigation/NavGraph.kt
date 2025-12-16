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
import com.example.uventapp.ui.screen.documentation.AddDocumentationScreen
import com.example.uventapp.ui.screen.documentation.AllDocumentationScreen
import com.example.uventapp.ui.screen.profile.ProfileScreen
import com.example.uventapp.ui.screen.profile.ProfileViewModel
import com.example.uventapp.ui.screen.participants.ParticipantListScreen // IMPORT BARU

@Composable
fun NavGraph(navController: NavHostController) {

    // Inisialisasi kedua ViewModel di sini
    val eventViewModel: EventManagementViewModel = viewModel()
    val profileViewModel: ProfileViewModel = viewModel()

    NavHost(navController = navController, startDestination = Screen.Splash.route) {

        composable(Screen.Splash.route) { SplashScreen(navController) }
        composable(Screen.Login.route) { LoginScreen(navController, profileViewModel) }
        composable(Screen.Register.route) { RegistrationScreen(navController, profileViewModel) }
        composable(Screen.Home.route) { HomeScreen(navController) }
        composable(Screen.EventList.route) { EventListScreen(navController, eventViewModel, profileViewModel) }

        composable(
            route = Screen.DetailEvent.route,
            arguments = listOf(navArgument("eventId") { type = NavType.IntType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getInt("eventId")
            DetailEventScreen(navController, eventId, eventViewModel, profileViewModel)
        }

        composable(
            route = Screen.RegistrationFormScreen.route,
            arguments = listOf(navArgument("eventId") { type = NavType.IntType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getInt("eventId")
            RegistrationFormScreen(navController, eventViewModel, profileViewModel, eventId)
        }

        composable(
            route = Screen.EditRegistration.route,
            arguments = listOf(navArgument("eventId") { type = NavType.IntType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getInt("eventId")
            EditRegistrationScreen(navController, eventViewModel, eventId)
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
            // --- PERBAIKAN DI SINI ---
            // Tambahkan 'profileViewModel = profileViewModel'
            MyRegisteredEventScreen(
                navController = navController,
                viewModel = eventViewModel,
                profileViewModel = profileViewModel, // <-- INI YANG HILANG
                eventName = eventName
            )
        }

        composable(Screen.AddEvent.route) {
            // (Panggilan ini sudah benar dari sebelumnya)
            AddEventScreen(
                navController = navController,
                viewModel = eventViewModel,
                profileViewModel = profileViewModel
            )
        }

        composable(
            route = Screen.EditEvent.route,
            arguments = listOf(navArgument("eventId") { type = NavType.IntType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getInt("eventId")
            EditEventScreen(navController, eventViewModel, eventId)
        }

        // (Rute AddFeedback, AllFeedback, AllDocumentation, AddDocumentation tetap sama)
        composable(
            route = Screen.AddFeedback.route,
            arguments = listOf(navArgument("eventId") { type = NavType.IntType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getInt("eventId")
            AddFeedbackScreen(navController, eventViewModel, profileViewModel, eventId)
        }
        composable(
            route = Screen.AllFeedback.route,
            arguments = listOf(navArgument("eventId") { type = NavType.IntType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getInt("eventId")
            AllFeedbackScreen(navController, eventViewModel, profileViewModel, eventId)
        }
        composable(
            route = Screen.AllDocumentation.route,
            arguments = listOf(navArgument("eventId") { type = NavType.IntType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getInt("eventId")
            AllDocumentationScreen(navController, eventViewModel, eventId)
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
            AddDocumentationScreen(navController, eventViewModel, profileViewModel, eventId, docId)
        }

        composable(Screen.Profile.route) {
            ProfileScreen(navController, profileViewModel)
        }

        composable(Screen.Notifications.route) {
            com.example.uventapp.ui.screen.notification.NotificationScreen(navController)
        }

        // ===== FITUR BARU: Participant List Screen =====
        composable(
            route = Screen.ParticipantList.route,
            arguments = listOf(
                navArgument("eventId") { type = NavType.IntType },
                navArgument("eventTitle") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getInt("eventId") ?: 0
            val eventTitle = backStackEntry.arguments?.getString("eventTitle") ?: ""
            ParticipantListScreen(navController, eventId, eventTitle)
        }
        // ===============================================
    }
}
