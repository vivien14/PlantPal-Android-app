package com.example.plantpal

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.plantpal.notification.NotificationHelper
import com.example.plantpal.ui.navigation.NavGraph
import com.example.plantpal.ui.navigation.Screen
import com.example.plantpal.ui.theme.PlantPalTheme
import com.example.plantpal.ui.viewmodel.PlantViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            PlantPalTheme(dynamicColor = false) {
                val navController = rememberNavController()
                val plantViewModel: PlantViewModel = viewModel()

                // Handle notification deep link
                LaunchedEffect(intent) {
                    handleNotificationIntent(intent, navController)
                }

                NavGraph(
                    navController = navController,
                    plantViewModel = plantViewModel
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun handleNotificationIntent(
        intent: Intent?,
        navController: androidx.navigation.NavHostController
    ) {
        intent?.extras?.getLong(NotificationHelper.EXTRA_PLANT_ID)?.let { plantId ->
            if (plantId > 0) {
                navController.navigate(Screen.PlantDetail.createRoute(plantId))
            }
        }
    }
}