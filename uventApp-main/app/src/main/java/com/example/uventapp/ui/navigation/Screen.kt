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

    object EditRegistration : Screen("edit_registration/{eventId}") {
        fun createRoute(eventId: Int) = "edit_registration/$eventId"
    }

    object MyRegisteredEvent : Screen("my_events?eventName={eventName}") {
        fun createRoute(eventName: String) = "my_events?eventName=$eventName"
    }

    object AddEvent : Screen("add_event")
    object EditEvent : Screen("edit_event/{eventId}") {
        fun createRoute(eventId: Int) = "edit_event/$eventId"
    }

    object AddFeedback : Screen("add_feedback/{eventId}") {
        fun createRoute(eventId: Int) = "add_feedback/$eventId"
    }

    object AllFeedback : Screen("all_feedback/{eventId}") {
        fun createRoute(eventId: Int) = "all_feedback/$eventId"
    }

    object AllDocumentation : Screen("all_documentation/{eventId}") {
        fun createRoute(eventId: Int) = "all_documentation/$eventId"
    }

    // --- PERBAIKAN DI SINI ---
    object AddDocumentation : Screen("add_documentation/{eventId}?docId={docId}") {
        // Rute untuk Tambah Baru
        fun createRoute(eventId: Int) = "add_documentation/$eventId"

        // Rute untuk Edit
        fun createEditRoute(eventId: Int, docId: Int) = "add_documentation/$eventId?docId=$docId"
    }
    // -------------------------

    // --- RUTE BARU DITAMBAHKAN ---
    object Profile : Screen("profile")
    // ---------------------------
}
