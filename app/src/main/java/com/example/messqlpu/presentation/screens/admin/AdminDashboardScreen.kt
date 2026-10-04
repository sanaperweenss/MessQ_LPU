package com.example.messqlpu.presentation.screens.admin

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.messqlpu.domain.model.CrowdLevel
import com.example.messqlpu.presentation.components.MessQButton
import com.example.messqlpu.presentation.components.MessQTopBar
import com.example.messqlpu.presentation.components.ModernCard
import com.example.messqlpu.presentation.viewmodel.AdminViewModel
import com.example.messqlpu.ui.theme.PrimaryPurple
import com.example.messqlpu.ui.theme.SuccessGreen
import com.example.messqlpu.ui.theme.WarningAmber

@Composable
fun AdminDashboardScreen(
    onBackClick: () -> Unit,
    viewModel: AdminViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val metrics by viewModel.metrics.collectAsState()
    val diningHalls by viewModel.diningHalls.collectAsState()

    var activeManagementTab by remember { mutableIntStateOf(0) } // 0: Queues, 1: Dining Halls, 2: Analytics

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            MessQTopBar(
                title = "Dining Hall Admin Portal",
                onBackClick = onBackClick
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // DASHBOARD METRICS GRID
            Text(
                text = "Live Campus Dining Metrics",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AdminMetricTile(
                    title = "Active Queues",
                    value = "${metrics.activeQueues}",
                    subtitle = "All dining spots",
                    color = PrimaryPurple,
                    modifier = Modifier.weight(1f)
                )
                AdminMetricTile(
                    title = "Students Waiting",
                    value = "${metrics.studentsWaiting}",
                    subtitle = "Across counters",
                    color = WarningAmber,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AdminMetricTile(
                    title = "Orders Today",
                    value = "${metrics.ordersToday}",
                    subtitle = "Fast pre-orders",
                    color = SuccessGreen,
                    modifier = Modifier.weight(1f)
                )
                AdminMetricTile(
                    title = "Overall Capacity",
                    value = "${metrics.diningHallCapacityPercent}%",
                    subtitle = "Occupancy rate",
                    color = Color(0xFF0EA5E9),
                    modifier = Modifier.weight(1f)
                )
            }

            // MANAGEMENT TABS (Queues, Dining Locations, Analytics)
            SecondaryScrollableTabRow(
                selectedTabIndex = activeManagementTab,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = PrimaryPurple,
                indicator = {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(activeManagementTab),
                        color = PrimaryPurple,
                        height = 3.dp
                    )
                }
            ) {
                Tab(
                    selected = activeManagementTab == 0,
                    onClick = { activeManagementTab = 0 },
                    text = {
                        Text(
                            "Queues Controller",
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                )
                Tab(
                    selected = activeManagementTab == 1,
                    onClick = { activeManagementTab = 1 },
                    text = {
                        Text(
                            "Hall Capacity",
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                )
                Tab(
                    selected = activeManagementTab == 2,
                    onClick = { activeManagementTab = 2 },
                    text = {
                        Text(
                            "Analytics Charts",
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                )
            }

            when (activeManagementTab) {
                0 -> { // QUEUES CONTROLLER
                    ModernCard {
                        Text(
                            text = "Counter Dispense Action",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Advance token numbers as meals are served at the counter",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        diningHalls.forEach { hall ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = hall.name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                    Text(
                                        text = "Current Wait: ~${hall.waitTimeMinutes} mins • ${hall.crowdLevel.label}",
                                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                }

                                Button(
                                    onClick = { viewModel.advanceToken(hall.id) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                                ) {
                                    Icon(Icons.Default.SkipNext, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Next Token")
                                }
                            }
                            HorizontalDivider()
                        }
                    }
                }

                1 -> { // HALL CAPACITY MANAGEMENT
                    ModernCard {
                        Text(
                            text = "Update Live Crowd Status",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Broadcast live hall congestion to campus students",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        diningHalls.forEach { hall ->
                            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                Text(text = hall.name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    CrowdLevel.values().forEach { level ->
                                        val isCurrent = hall.crowdLevel == level
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isCurrent) level.badgeColor else MaterialTheme.colorScheme.surfaceVariant,
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable {
                                                    val wait = when (level) {
                                                        CrowdLevel.LOW -> 5
                                                        CrowdLevel.MODERATE -> 15
                                                        CrowdLevel.HIGH -> 28
                                                        CrowdLevel.SEVERE -> 40
                                                    }
                                                    viewModel.updateCrowd(hall.id, level, wait)
                                                }
                                        ) {
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier.padding(vertical = 8.dp)
                                            ) {
                                                Text(
                                                    text = level.name.take(3),
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = if (isCurrent) Color.White else MaterialTheme.colorScheme.onSurface,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            HorizontalDivider()
                        }
                    }
                }

                2 -> { // ANALYTICS CHARTS (Crowd Trends, Peak Hours, Most Ordered Foods)
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Peak Hours Chart Card
                        ModernCard {
                            Text(
                                text = "Peak Dining Hours (Campus Trends)",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = metrics.peakHoursAlert,
                                style = MaterialTheme.typography.bodySmall.copy(color = PrimaryPurple, fontWeight = FontWeight.SemiBold)
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            // Custom Bar Chart Canvas
                            val barData = listOf(
                                "8 AM" to 0.35f,
                                "10 AM" to 0.20f,
                                "12 PM" to 0.70f,
                                "1 PM" to 0.95f,
                                "2 PM" to 0.85f,
                                "4 PM" to 0.30f,
                                "7 PM" to 0.75f,
                                "8 PM" to 0.90f
                            )

                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                            ) {
                                val barWidth = size.width / (barData.size * 2)
                                barData.forEachIndexed { i, pair ->
                                    val barHeight = size.height * pair.second
                                    val left = i * (barWidth * 2) + barWidth / 2
                                    val top = size.height - barHeight
                                    drawRoundRect(
                                        color = if (pair.second > 0.8f) Color(0xFFEF4444) else PrimaryPurple,
                                        topLeft = Offset(left, top),
                                        size = Size(barWidth, barHeight),
                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                barData.forEach {
                                    Text(
                                        text = it.first,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 9.sp
                                        )
                                    )
                                }
                            }
                        }

                        // Top Selling Foods Card
                        ModernCard {
                            Text(
                                text = "Most Ordered Campus Dishes",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            listOf(
                                "Rajma Rice Special" to "412 orders today (48%)",
                                "Paneer Butter Masala Combo" to "280 orders today (32%)",
                                "Mysore Masala Dosa" to "195 orders today (24%)",
                                "Cold Coffee Frappe" to "310 orders today (36%)"
                            ).forEach { (dish, stats) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = dish, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                                    Text(text = stats, style = MaterialTheme.typography.bodySmall.copy(color = PrimaryPurple))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun AdminMetricTile(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = color
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }
    }
}
