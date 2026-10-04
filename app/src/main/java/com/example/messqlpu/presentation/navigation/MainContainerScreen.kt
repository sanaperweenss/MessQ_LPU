package com.example.messqlpu.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.messqlpu.MessQApplication
import com.example.messqlpu.presentation.screens.explore.ExploreScreen
import com.example.messqlpu.presentation.screens.home.HomeScreen
import com.example.messqlpu.presentation.screens.orders.OrderHistoryScreen
import com.example.messqlpu.presentation.screens.profile.ProfileScreen
import com.example.messqlpu.presentation.screens.queue.LiveQueueScreen
import com.example.messqlpu.ui.theme.PrimaryPurple

sealed class BottomNavItem(
    val title: String,
    val icon: ImageVector,
    val screen: Screen
) {
    object Home : BottomNavItem("Home", Icons.Default.Home, Screen.Home)
    object Explore : BottomNavItem("Explore", Icons.Default.Explore, Screen.Explore)
    object Queue : BottomNavItem("Queue", Icons.Default.ConfirmationNumber, Screen.Queue)
    object Orders : BottomNavItem("Orders", Icons.Default.ReceiptLong, Screen.Orders)
    object Profile : BottomNavItem("Profile", Icons.Default.Person, Screen.Profile)
}

@Composable
fun MainContainerScreen(
    initialTabRoute: String = Screen.Home.route,
    onNavigateToMenu: (String) -> Unit,
    onNavigateToQueueTimeline: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToDiningDetail: (String) -> Unit,
    onNavigateToFoodDetail: (String) -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToAiAssistant: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    onLogout: () -> Unit
) {
    val defaultTab = remember(initialTabRoute) {
        when (initialTabRoute) {
            Screen.Orders.route, "orders" -> BottomNavItem.Orders
            Screen.Queue.route, "queue" -> BottomNavItem.Queue
            Screen.Explore.route, "explore" -> BottomNavItem.Explore
            Screen.Profile.route, "profile" -> BottomNavItem.Profile
            else -> BottomNavItem.Home
        }
    }
    var selectedTab by remember(initialTabRoute) { mutableStateOf<BottomNavItem>(defaultTab) }

    val navItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Explore,
        BottomNavItem.Queue,
        BottomNavItem.Orders,
        BottomNavItem.Profile
    )

    // Check if active queue exists to show badge
    val queueRepo = MessQApplication.instance.container.queueRepository
    val activeToken by queueRepo.activeTokenFlow.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            ) {
                navItems.forEach { item ->
                    val isSelected = selectedTab == item

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = item },
                        icon = {
                            if (item == BottomNavItem.Queue && activeToken != null) {
                                BadgedBox(
                                    badge = {
                                        Badge(containerColor = PrimaryPurple) {
                                            Text("#${activeToken?.tokenNumber}")
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.title
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title
                                )
                            }
                        },
                        label = {
                            Text(
                                text = item.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryPurple,
                            selectedTextColor = PrimaryPurple,
                            indicatorColor = PrimaryPurple.copy(alpha = 0.12f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (selectedTab) {
                BottomNavItem.Home -> HomeScreen(
                    onNavigateToMenu = onNavigateToMenu,
                    onNavigateToQueue = { selectedTab = BottomNavItem.Queue },
                    onNavigateToMap = onNavigateToMap,
                    onNavigateToOrderFood = { selectedTab = BottomNavItem.Explore },
                    onNavigateToDiningDetail = onNavigateToDiningDetail,
                    onNavigateToFoodDetail = onNavigateToFoodDetail,
                    onNavigateToAiAssistant = onNavigateToAiAssistant,
                    onNavigateToNotifications = onNavigateToNotifications
                )

                BottomNavItem.Explore -> ExploreScreen(
                    onNavigateToDiningDetail = onNavigateToDiningDetail
                )

                BottomNavItem.Queue -> LiveQueueScreen(
                    onNavigateToExplore = { selectedTab = BottomNavItem.Explore },
                    onNavigateToProgressTimeline = onNavigateToQueueTimeline
                )

                BottomNavItem.Orders -> OrderHistoryScreen(
                    onNavigateToMenu = { onNavigateToMenu("") }
                )

                BottomNavItem.Profile -> ProfileScreen(
                    onNavigateToAdmin = onNavigateToAdmin,
                    onLogout = onLogout
                )
            }
        }
    }
}
