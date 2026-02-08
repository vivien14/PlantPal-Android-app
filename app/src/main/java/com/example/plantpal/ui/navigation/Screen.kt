package com.example.plantpal.ui.navigation

sealed class Screen(val route: String) {
    object PlantList : Screen("plant_list")
    object AddEditPlant : Screen("add_edit_plant/{plantId}") {
        fun createRoute(plantId: Long? = null): String {
            return if (plantId != null) {
                "add_edit_plant/$plantId"
            } else {
                "add_edit_plant/-1"
            }
        }
    }
    object PlantDetail : Screen("plant_detail/{plantId}") {
        fun createRoute(plantId: Long): String = "plant_detail/$plantId"
    }
    object About : Screen("about")
}
