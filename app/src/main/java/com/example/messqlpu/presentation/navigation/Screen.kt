package com.example.messqlpu.presentation.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object MainContainer : Screen("main_container?tab={tab}") {
        fun createRoute(tab: String = "home") = "main_container?tab=$tab"
    }
    
    // Bottom navigation sub-routes
    object Home : Screen("home")
    object Explore : Screen("explore")
    object Queue : Screen("queue")
    object Orders : Screen("orders")
    object Profile : Screen("profile")

    // Fullscreen secondary screens
    object DiningDetail : Screen("dining_detail/{hallId}") {
        fun createRoute(hallId: String) = "dining_detail/$hallId"
    }
    object QueueProgress : Screen("queue_progress")
    object Menu : Screen("menu/{hallId}") {
        fun createRoute(hallId: String = "") = if (hallId.isNotEmpty()) "menu/$hallId" else "menu/all"
    }
    object FoodDetail : Screen("food_detail/{foodId}") {
        fun createRoute(foodId: String) = "food_detail/$foodId"
    }
    object Cart : Screen("cart")
    object OrderSuccess : Screen("order_success/{orderNumber}/{pickupTime}") {
        fun createRoute(orderNumber: String, pickupTime: String) = "order_success/$orderNumber/$pickupTime"
    }
    object CampusMap : Screen("campus_map")
    object AiDiningAssistant : Screen("ai_assistant")
    object NotificationCenter : Screen("notifications")
    object AdminDashboard : Screen("admin_dashboard")
}
