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
    object MyRegisteredEvent : Screen("my_registered_event/{eventName}") {
        fun createRoute(eventName: String) = "my_registered_event/$eventName"
    }
}
