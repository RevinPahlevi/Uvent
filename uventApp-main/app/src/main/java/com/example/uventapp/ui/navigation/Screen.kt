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

    object RegistrationFormScreen : Screen("registration_form/{eventName}") {
        fun createRoute(eventName: String) = "registration_form/$eventName"
    }

    object EditRegistration : Screen("edit_registration/{eventName}") {
        fun createRoute(eventName: String) = "edit_registration/$eventName"
    }

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