package com.example.uventapp.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.uventapp.ui.theme.PrimaryGreen

data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

@Composable
fun BottomNavBar(navController: NavController) {
    val items = listOf(
        BottomNavItem("home", Icons.Filled.Home, "Home"),
        // --- PERBAIKAN 1: Gunakan rute dasar untuk 'my_events' ---
        BottomNavItem("my_events", Icons.Filled.EventNote, "Event Saya"),
        BottomNavItem("notifications", Icons.Filled.Notifications, "Notifikasi"),
        BottomNavItem("profile", Icons.Filled.Person, "Akun"),
    )

    NavigationBar(
        modifier = Modifier.padding(top = 1.dp),
        containerColor = androidx.compose.ui.graphics.Color.White,
        tonalElevation = 5.dp
    ) {
        // --- PERBAIKAN 2: Gunakan state untuk mendapatkan rute saat ini ---
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            // --- PERBAIKAN 3: Cek jika rute saat ini DIMULAI DENGAN rute item ---
            // Ini akan menangani rute dengan argumen seperti "my_events?eventName=..."
            val isSelected = currentRoute?.startsWith(item.route) == true

            NavigationBarItem(
                icon = {
                    Icon(
                        item.icon,
                        contentDescription = item.label,
                    )
                },
                label = { Text(item.label) },
                selected = isSelected,
                // --- PERBAIKAN 4: Logika onClick yang lebih andal ---
                onClick = {
                    navController.navigate(item.route) {
                        // Pop up ke awal grafik navigasi untuk menghindari tumpukan destinasi
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // Hindari membuat salinan ganda dari destinasi yang sama
                        launchSingleTop = true
                        // Pulihkan state saat memilih kembali item yang sebelumnya dipilih
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryGreen,
                    selectedTextColor = PrimaryGreen,
                    indicatorColor = androidx.compose.ui.graphics.Color.Transparent
                )
            )
        }
    }
}