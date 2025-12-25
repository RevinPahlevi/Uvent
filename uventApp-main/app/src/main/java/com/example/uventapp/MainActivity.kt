package com.example.uventapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.example.uventapp.ui.navigation.NavGraph
import com.example.uventapp.ui.navigation.Screen
import com.example.uventapp.ui.theme.LightBackground
import com.example.uventapp.ui.theme.UVentAppTheme

class MainActivity : ComponentActivity() {
    
    companion object {
        private const val TAG = "MainActivity"
    }
    
    private var navigateTo: String? = null
    private var eventId: Int = 0
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d(TAG, "Notification permission granted")
        } else {
            Log.d(TAG, "Notification permission denied")
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        requestNotificationPermission()
        
        handleNotificationIntent()
        
        setContent {
            val navController = rememberNavController()

            UVentAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = LightBackground
                ) {
                    NavGraph(navController = navController)
                    
                    LaunchedEffect(navigateTo, eventId) {
                        if (navigateTo != null && eventId > 0) {
                            Log.d(TAG, "Deep link navigation: $navigateTo, eventId: $eventId")
                            
                            kotlinx.coroutines.delay(500)
                            
                            when (navigateTo) {
                                "feedback" -> {
                                    navController.navigate(Screen.AddFeedback.createRoute(eventId)) {
                                        popUpTo(Screen.Home.route) { inclusive = false }
                                    }
                                }
                                "event_detail" -> {
                                    navController.navigate(Screen.DetailEvent.createRoute(eventId)) {
                                        popUpTo(Screen.Home.route) { inclusive = false }
                                    }
                                }
                            }
                            
                            navigateTo = null
                            eventId = 0
                        }
                    }
                }
            }
        }
    }
    
    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent()
    }
    
    private fun handleNotificationIntent() {
        navigateTo = intent.getStringExtra("navigate_to")
        eventId = intent.getIntExtra("event_id", 0)
        
        if (navigateTo != null) {
            Log.d(TAG, "Received notification intent: navigate_to=$navigateTo, event_id=$eventId")
        }
    }
    
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Log.d(TAG, "Notification permission already granted")
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }
}
