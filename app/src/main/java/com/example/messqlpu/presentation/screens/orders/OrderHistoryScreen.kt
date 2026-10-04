package com.example.messqlpu.presentation.screens.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.messqlpu.domain.model.Order
import com.example.messqlpu.presentation.components.MessQBottomNavigation
import com.example.messqlpu.presentation.navigation.Screen
import com.example.messqlpu.presentation.viewmodel.OrderViewModel
import com.example.messqlpu.ui.theme.AccentGold
import com.example.messqlpu.ui.theme.PrimaryPurple
import com.example.messqlpu.ui.theme.SuccessGreen
import com.example.messqlpu.ui.theme.WarningAmber

@Composable
fun OrderHistoryScreen(
    onNavigateToMenu: () -> Unit,
    onBottomNavNavigate: (String) -> Unit = {},
    viewModel: OrderViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val activeOrders by viewModel.activeOrders.collectAsState()
    val pastOrders by viewModel.pastOrders.collectAsState()
    var selectedTabIndex by remember { mutableIntStateOf(0) } // Default to 0 (Active Tab)

    var ratingOrderTarget by remember { mutableStateOf<Order?>(null) }
    var selectedStars by remember { mutableIntStateOf(5) }

    if (ratingOrderTarget != null) {
        AlertDialog(
            onDismissRequest = { ratingOrderTarget = null },
            title = { Text("Rate Meal Experience", fontWeight = FontWeight.Bold) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("How was ${ratingOrderTarget?.items?.firstOrNull()?.foodItem?.name}?")
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        (1..5).forEach { star ->
                            IconButton(onClick = { selectedStars = star }) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = if (star <= selectedStars) AccentGold else Color(0xFFCBD5E1),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        ratingOrderTarget?.let { viewModel.rateOrder(it.orderId, selectedStars) }
                        ratingOrderTarget = null
                    }
                ) {
                    Text("Submit Rating", fontWeight = FontWeight.Bold, color = PrimaryPurple)
                }
            },
            dismissButton = {
                TextButton(onClick = { ratingOrderTarget = null }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "My Orders",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        fontSize = 20.sp
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Pill segmented toggle: [ Active ]  [ History ]
                Surface(
                    shape = RoundedCornerShape(25.dp),
                    color = Color(0xFFE2E8F0).copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(4.dp)
                    ) {
                        // Active Tab
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clickable { selectedTabIndex = 0 },
                            shape = RoundedCornerShape(20.dp),
                            color = if (selectedTabIndex == 0) PrimaryPurple else Color.Transparent
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "Active",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedTabIndex == 0) Color.White else Color(0xFF64748B),
                                        fontSize = 14.sp
                                    )
                                )
                            }
                        }

                        // History Tab
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clickable { selectedTabIndex = 1 },
                            shape = RoundedCornerShape(20.dp),
                            color = if (selectedTabIndex == 1) PrimaryPurple else Color.Transparent
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "History",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedTabIndex == 1) Color.White else Color(0xFF64748B),
                                        fontSize = 14.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        val currentOrders = if (selectedTabIndex == 0) activeOrders else pastOrders

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 20.dp)
        ) {
            if (currentOrders.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.res.painterResource(id = com.example.messqlpu.R.drawable.messq_mascot),
                            contentDescription = "No Orders",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(22.dp)),
                            contentScale = ContentScale.Fit
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = if (selectedTabIndex == 0) "No Active Orders 🛒" else "No Order History",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (selectedTabIndex == 0) "Place an order from campus dining menus and your live pickup pass will appear here!" else "Past completed orders will appear here.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            ),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            } else {
                items(currentOrders) { order ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Header Row: Order Number & Status Badge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = PrimaryPurple.copy(alpha = 0.12f),
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = PrimaryPurple,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    val cleanNum = if (order.orderNumber.startsWith("#")) order.orderNumber else "#${order.orderNumber}"
                                    Text(
                                        text = "Order $cleanNum",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A),
                                            fontSize = 15.sp
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${order.diningHallName} • ⏰ ${order.pickupTime}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = PrimaryPurple,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            val (statusText, statusBg, statusColor) = when (order.status) {
                                com.example.messqlpu.domain.model.OrderStatus.PREPARING -> Triple("Preparing", WarningAmber.copy(alpha = 0.15f), WarningAmber)
                                com.example.messqlpu.domain.model.OrderStatus.READY_FOR_PICKUP -> Triple("Ready for Pickup", SuccessGreen.copy(alpha = 0.15f), SuccessGreen)
                                else -> Triple("Confirmed", PrimaryPurple.copy(alpha = 0.15f), PrimaryPurple)
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = statusBg
                            ) {
                                Text(
                                    text = statusText,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = statusColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    ),
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF1F5F9))

                        // Ordered Items Breakdown List
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            order.items.forEach { cartItem ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = cartItem.foodItem.imageUrl,
                                        contentDescription = cartItem.foodItem.name,
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(10.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = cartItem.foodItem.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1E293B),
                                                fontSize = 14.sp
                                            )
                                        )
                                        Text(
                                            text = "Qty: ${cartItem.quantity} • ₹${cartItem.foodItem.price.toInt()} base",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Color(0xFF64748B),
                                                fontSize = 12.sp
                                            )
                                        )
                                        if (cartItem.selectedAddOns.isNotEmpty()) {
                                            cartItem.selectedAddOns.forEach { addOn ->
                                                Text(
                                                    text = "+ ${addOn.name} (+₹${addOn.price.toInt()})",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = PrimaryPurple,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 11.sp
                                                    )
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = "₹${cartItem.itemTotalPrice.toInt()}",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A),
                                            fontSize = 14.sp
                                        )
                                    )
                                }
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF1F5F9))

                        // Pickup Verification Pass Box
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = PrimaryPurple.copy(alpha = 0.08f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryPurple.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Pass",
                                        tint = PrimaryPurple,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    val cleanCodeNum = if (order.orderNumber.startsWith("#")) order.orderNumber else "#${order.orderNumber}"
                                    Text(
                                        text = "Pickup Code: $cleanCodeNum",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = PrimaryPurple,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    )
                                }
                                Text(
                                    text = "Total: ₹${order.totalAmount.toInt()}",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A),
                                        fontSize = 15.sp
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Action Buttons: [ Reorder ]  [ ★ Rate ]
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .clickable {
                                        viewModel.reorder(order) {
                                            selectedTabIndex = 0
                                        }
                                    },
                                shape = RoundedCornerShape(12.dp),
                                color = Color.Transparent,
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "Reorder Items",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF334155),
                                            fontSize = 13.sp
                                        )
                                    )
                                }
                            }

                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .clickable { ratingOrderTarget = order },
                                shape = RoundedCornerShape(12.dp),
                                color = Color.Transparent,
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = AccentGold,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Rate Meal",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF334155),
                                            fontSize = 13.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
}
