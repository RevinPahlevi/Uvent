package com.example.uventapp.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object EventList : Screen("event_list")

    object DetailEvent : Screen("detail_event/{eventId}") {
        fun createRoute(eventId: Int) = "detail_event/$eventId"
    }

    object RegistrationFormScreen : Screen("registration_form/{eventId}") {
        fun createRoute(eventId: Int) = "registration_form/$eventId"
    }

    // --- PERBAIKAN DI SINI ---
    // Ubah dari "eventName" (String) ke "eventId" (Int)
    object EditRegistration : Screen("edit_registration/{eventId}") {
        fun createRoute(eventId: Int) = "edit_registration/$eventId"
    }
    // -------------------------

    // Ini adalah satu-satunya layar "Event Saya"
    object MyRegisteredEvent : Screen("my_events?eventName={eventName}") {
        fun createRoute(eventName: String) = "my_events?eventName=$eventName"
    }

    // Rute untuk membuat/mengedit event
    object AddEvent : Screen("add_event")
    object EditEvent : Screen("edit_event/{eventId}") {
        fun createRoute(eventId: Int) = "edit_event/$eventId"
    }
}