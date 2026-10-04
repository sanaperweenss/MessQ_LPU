package com.example.messqlpu.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.messqlpu.presentation.screens.admin.AdminDashboardScreen
import com.example.messqlpu.presentation.screens.ai.AiDiningAssistantScreen
import com.example.messqlpu.presentation.screens.auth.LoginScreen
import com.example.messqlpu.presentation.screens.cart.CartScreen
import com.example.messqlpu.presentation.screens.cart.OrderSuccessScreen
import com.example.messqlpu.presentation.screens.dining.DiningDetailScreen
import com.example.messqlpu.presentation.screens.map.CampusMapScreen
import com.example.messqlpu.presentation.screens.menu.FoodDetailScreen
import com.example.messqlpu.presentation.screens.menu.MenuScreen
import com.example.messqlpu.presentation.screens.notifications.NotificationCenterScreen
import com.example.messqlpu.presentation.screens.onboarding.OnboardingScreen
import com.example.messqlpu.presentation.screens.queue.QueueProgressScreen
import com.example.messqlpu.presentation.screens.splash.SplashScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // SCREEN 1: SPLASH
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateNext = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // SCREEN 2: ONBOARDING
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinishOnboarding = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        // SCREEN 3: LOGIN
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.MainContainer.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // SCREEN 4, 5, 7, 15, 16: MAIN CONTAINER WITH BOTTOM NAV
        composable(
            route = Screen.MainContainer.route,
            arguments = listOf(
                navArgument("tab") {
                    type = NavType.StringType
                    defaultValue = "home"
                }
            )
        ) { backStackEntry ->
            val initialTab = backStackEntry.arguments?.getString("tab") ?: "home"
            MainContainerScreen(
                initialTabRoute = initialTab,
                onNavigateToMenu = { hallId ->
                    navController.navigate(Screen.Menu.createRoute(hallId))
                },
                onNavigateToQueueTimeline = {
                    navController.navigate(Screen.QueueProgress.route)
                },
                onNavigateToMap = {
                    navController.navigate(Screen.CampusMap.route)
                },
                onNavigateToDiningDetail = { hallId ->
                    navController.navigate(Screen.DiningDetail.createRoute(hallId))
                },
                onNavigateToFoodDetail = { foodId ->
                    navController.navigate(Screen.FoodDetail.createRoute(foodId))
                },
                onNavigateToCart = {
                    navController.navigate(Screen.Cart.route)
                },
                onNavigateToAiAssistant = {
                    navController.navigate(Screen.AiDiningAssistant.route)
                },
                onNavigateToNotifications = {
                    navController.navigate(Screen.NotificationCenter.route)
                },
                onNavigateToAdmin = {
                    navController.navigate(Screen.AdminDashboard.route)
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.MainContainer.route) { inclusive = true }
                    }
                }
            )
        }

        // SCREEN 6: DINING HALL DETAILS
        composable(
            route = Screen.DiningDetail.route,
            arguments = listOf(navArgument("hallId") { type = NavType.StringType })
        ) { backStackEntry ->
            val hallId = backStackEntry.arguments?.getString("hallId") ?: "dh_1"
            DiningDetailScreen(
                hallId = hallId,
                onBackClick = { navController.popBackStack() },
                onNavigateToQueue = {
                    navController.navigate(Screen.MainContainer.route)
                },
                onNavigateToFoodDetail = { foodId ->
                    navController.navigate(Screen.FoodDetail.createRoute(foodId))
                }
            )
        }

        // SCREEN 8: QUEUE PROGRESS
        composable(Screen.QueueProgress.route) {
            QueueProgressScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // SCREEN 9: MENU
        composable(
            route = Screen.Menu.route,
            arguments = listOf(navArgument("hallId") { type = NavType.StringType })
        ) { backStackEntry ->
            val hallId = backStackEntry.arguments?.getString("hallId") ?: ""
            val normalizedId = if (hallId == "all") "" else hallId
            MenuScreen(
                hallId = normalizedId,
                onBackClick = { navController.popBackStack() },
                onNavigateToFoodDetail = { foodId ->
                    navController.navigate(Screen.FoodDetail.createRoute(foodId))
                },
                onNavigateToCart = {
                    navController.navigate(Screen.Cart.route)
                }
            )
        }

        // SCREEN 10: FOOD DETAILS
        composable(
            route = Screen.FoodDetail.route,
            arguments = listOf(navArgument("foodId") { type = NavType.StringType })
        ) { backStackEntry ->
            val foodId = backStackEntry.arguments?.getString("foodId") ?: "f_1"
            FoodDetailScreen(
                foodId = foodId,
                onBackClick = { navController.popBackStack() },
                onCartUpdated = {
                    navController.navigate(Screen.Cart.route)
                }
            )
        }

        // SCREEN 11: CART
        composable(Screen.Cart.route) {
            CartScreen(
                onBackClick = { navController.popBackStack() },
                onOrderSuccess = { order ->
                    navController.navigate(Screen.OrderSuccess.createRoute(order.orderNumber, order.pickupTime)) {
                        popUpTo(Screen.Cart.route) { inclusive = true }
                    }
                },
                onExploreMenu = {
                    navController.popBackStack()
                }
            )
        }

        // SCREEN 12: ORDER SUCCESS
        composable(
            route = Screen.OrderSuccess.route,
            arguments = listOf(
                navArgument("orderNumber") { type = NavType.StringType },
                navArgument("pickupTime") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val orderNumber = backStackEntry.arguments?.getString("orderNumber") ?: "MQ-8921"
            val pickupTime = backStackEntry.arguments?.getString("pickupTime") ?: "12:45 PM"
            OrderSuccessScreen(
                orderNumber = orderNumber,
                pickupTime = pickupTime,
                onViewOrder = {
                    navController.navigate(Screen.MainContainer.createRoute("orders")) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onContinueExploring = {
                    navController.navigate(Screen.MainContainer.createRoute("home")) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // SCREEN 13: CAMPUS MAP
        composable(Screen.CampusMap.route) {
            CampusMapScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToDiningDetail = { hallId ->
                    navController.navigate(Screen.DiningDetail.createRoute(hallId))
                },
                onBottomNavNavigate = {
                    navController.navigate(Screen.MainContainer.route)
                }
            )
        }

        // SCREEN 14: AI DINING ASSISTANT
        composable(Screen.AiDiningAssistant.route) {
            AiDiningAssistantScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToDiningDetail = { hallId ->
                    navController.navigate(Screen.DiningDetail.createRoute(hallId))
                },
                onNavigateToMenu = { hallId ->
                    navController.navigate(Screen.Menu.createRoute(hallId))
                },
                onBottomNavNavigate = {
                    navController.navigate(Screen.MainContainer.route)
                }
            )
        }

        // NOTIFICATION CENTER
        composable(Screen.NotificationCenter.route) {
            NotificationCenterScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // ADMIN MODULE DASHBOARD
        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
