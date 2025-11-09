package com.example.uventapp.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object RegistrationFormScreen : Screen("registration_form/{eventName}") {
        fun createRoute(eventName: String) = "registration_form/$eventName"
    }
    object EditRegistration : Screen("edit_registration/{eventName}") {
        fun createRoute(eventName: String) = "edit_registration/$eventName"
    }
    object Home : Screen("home")
    object EventList : Screen("event_list")
    object DetailEvent : Screen("detail_event/{eventId}") {
        fun createRoute(eventId: Int) = "detail_event/$eventId"
    }

    // --- INI BAGIAN YANG DIPERBAIKI ---
    // Rute diubah menjadi "my_events" agar sama dengan BottomNavBar.
    // eventName dijadikan parameter opsional (query parameter)
    object MyRegisteredEvent : Screen("my_events?eventName={eventName}") {
        // Fungsi createRoute diperbarui untuk membuat URL dengan parameter opsional
        fun createRoute(eventName: String) = "my_events?eventName=$eventName"
    }
    // ------------------------------------
}