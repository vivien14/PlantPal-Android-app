package com.example.plantpal.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.plantpal.ui.screens.AboutScreen
import com.example.plantpal.ui.screens.AddEditPlantScreen
import com.example.plantpal.ui.screens.PlantDetailScreen
import com.example.plantpal.ui.screens.PlantListScreen
import com.example.plantpal.ui.viewmodel.PlantViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    plantViewModel: PlantViewModel = viewModel()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.PlantList.route
    ) {
        composable(route = Screen.PlantList.route) {
            PlantListScreen(
                viewModel = plantViewModel,
                onPlantClick = { plantId ->
                    navController.navigate(Screen.PlantDetail.createRoute(plantId))
                },
                onAddPlantClick = {
                    navController.navigate(Screen.AddEditPlant.createRoute())
                },
                onAboutClick = {
                    navController.navigate(Screen.About.route)
                }
            )
        }

        composable(
            route = Screen.AddEditPlant.route,
            arguments = listOf(
                navArgument("plantId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val plantId = backStackEntry.arguments?.getLong("plantId") ?: -1L
            AddEditPlantScreen(
                viewModel = plantViewModel,
                plantId = if (plantId == -1L) null else plantId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.PlantDetail.route,
            arguments = listOf(
                navArgument("plantId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val plantId = backStackEntry.arguments?.getLong("plantId") ?: return@composable
            PlantDetailScreen(
                viewModel = plantViewModel,
                plantId = plantId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onEditClick = {
                    navController.navigate(Screen.AddEditPlant.createRoute(plantId))
                }
            )
        }

        composable(route = Screen.About.route) {
            AboutScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
