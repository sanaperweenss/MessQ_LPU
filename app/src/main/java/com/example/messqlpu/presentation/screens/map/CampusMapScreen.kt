package com.example.messqlpu.presentation.screens.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.messqlpu.domain.model.CrowdLevel
import com.example.messqlpu.presentation.components.MessQBottomNavigation
import com.example.messqlpu.presentation.navigation.Screen
import com.example.messqlpu.ui.theme.DangerRed
import com.example.messqlpu.ui.theme.PrimaryPurple
import com.example.messqlpu.ui.theme.SuccessGreen
import androidx.compose.ui.graphics.graphicsLayer
import com.example.messqlpu.ui.theme.WarningAmber

@Composable
fun CampusMapScreen(
    onBackClick: () -> Unit,
    onNavigateToDiningDetail: (String) -> Unit,
    onBottomNavNavigate: (String) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var zoomScale by remember { mutableFloatStateOf(1.0f) }

    Scaffold(
        containerColor = Color(0xFFF8F9FC),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .background(Color.White)
                    .padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF1E293B)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Campus Dining Map",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            fontSize = 19.sp
                        )
                    )
                }

                // Search Bar
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF1F5F9)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (searchQuery.isEmpty()) "Search locations..." else searchQuery,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = if (searchQuery.isEmpty()) Color(0xFF94A3B8) else Color(0xFF0F172A),
                                fontSize = 14.sp
                            )
                        )
                    }
                }
            }
        },
        bottomBar = {
            MessQBottomNavigation(
                currentRoute = Screen.Explore.route,
                onNavigate = onBottomNavNavigate
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // MAP CANVAS
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = zoomScale,
                        scaleY = zoomScale
                    )
            ) {
                val w = size.width
                val h = size.height

                // Campus grass/background
                drawRect(color = Color(0xFFE8F5E9).copy(alpha = 0.6f))

                // Campus blocks / lawns
                drawRect(
                    color = Color(0xFFDCEDC8).copy(alpha = 0.5f),
                    topLeft = Offset(w * 0.1f, h * 0.15f),
                    size = androidx.compose.ui.geometry.Size(w * 0.35f, h * 0.25f)
                )
                drawRect(
                    color = Color(0xFFDCEDC8).copy(alpha = 0.5f),
                    topLeft = Offset(w * 0.55f, h * 0.45f),
                    size = androidx.compose.ui.geometry.Size(w * 0.38f, h * 0.3f)
                )

                // Roads (light gray with white center dash)
                // Main avenue vertical
                drawLine(
                    color = Color(0xFFCBD5E1),
                    start = Offset(w * 0.48f, 0f),
                    end = Offset(w * 0.48f, h),
                    strokeWidth = 36f
                )
                drawLine(
                    color = Color.White,
                    start = Offset(w * 0.48f, 0f),
                    end = Offset(w * 0.48f, h),
                    strokeWidth = 4f
                )

                // Horizontal crossroad 1
                drawLine(
                    color = Color(0xFFCBD5E1),
                    start = Offset(0f, h * 0.35f),
                    end = Offset(w, h * 0.35f),
                    strokeWidth = 28f
                )

                // Horizontal crossroad 2
                drawLine(
                    color = Color(0xFFCBD5E1),
                    start = Offset(0f, h * 0.68f),
                    end = Offset(w, h * 0.68f),
                    strokeWidth = 26f
                )

                // Diagonal path
                drawLine(
                    color = Color(0xFFE2E8F0),
                    start = Offset(w * 0.1f, h * 0.85f),
                    end = Offset(w * 0.9f, h * 0.2f),
                    strokeWidth = 14f
                )
            }

            // MAP MARKER 1: Food Street (Red, 28 min)
            MapPillMarker(
                title = "Food Street",
                waitTime = "28 min",
                color = DangerRed,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 40.dp, end = 40.dp),
                onClick = { onNavigateToDiningDetail("dh_3") }
            )

            // MAP MARKER 2: Food Court A (Yellow, 12 min)
            MapPillMarker(
                title = "Food Court A",
                waitTime = "12 min",
                color = WarningAmber,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 110.dp, start = 30.dp),
                onClick = { onNavigateToDiningDetail("dh_2") }
            )

            // MAP MARKER 3: Main Dining Hall (Green, 5 min)
            MapPillMarker(
                title = "Main Dining Hall",
                waitTime = "5 min",
                color = SuccessGreen,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 10.dp, start = 40.dp),
                onClick = { onNavigateToDiningDetail("dh_1") }
            )

            // MAP MARKER 4: Café Express (Green, 4 min)
            MapPillMarker(
                title = "Café Express",
                waitTime = "4 min",
                color = SuccessGreen,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 120.dp, end = 20.dp),
                onClick = { onNavigateToDiningDetail("dh_4") }
            )

            // YOUR LOCATION PIN (Blue pulse circle)
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(top = 120.dp, start = 50.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF38BDF8).copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            modifier = Modifier.size(12.dp),
                            shape = CircleShape,
                            color = Color(0xFF0284C7),
                            border = androidx.compose.foundation.BorderStroke(2.dp, Color.White)
                        ) {}
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Your Location",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0284C7)
                        )
                    )
                }
            }

            // MAP CONTROLS (Right side: Target, Zoom +, Zoom -)
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 130.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .size(38.dp)
                        .clickable { zoomScale = 1.0f },
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White,
                    shadowElevation = 4.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "My Location",
                            tint = Color(0xFF475569),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White,
                    shadowElevation = 4.dp
                ) {
                    Column {
                        IconButton(
                            onClick = { zoomScale = (zoomScale + 0.2f).coerceAtMost(2.5f) },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Zoom In", tint = Color(0xFF475569), modifier = Modifier.size(18.dp))
                        }
                        HorizontalDivider(modifier = Modifier.width(38.dp), color = Color(0xFFE2E8F0))
                        IconButton(
                            onClick = { zoomScale = (zoomScale - 0.2f).coerceAtLeast(0.8f) },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Zoom Out", tint = Color(0xFF475569), modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            // BOTTOM LEGEND BAR
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(30.dp),
                color = Color.White,
                shadowElevation = 6.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendItem(color = SuccessGreen, text = "Low Crowd")
                    LegendItem(color = WarningAmber, text = "Moderate")
                    LegendItem(color = DangerRed, text = "High")
                }
            }
        }
    }
}

@Composable
private fun MapPillMarker(
    title: String,
    waitTime: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        shadowElevation = 5.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        fontSize = 12.sp
                    )
                )
                Text(
                    text = waitTime,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = color,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                color = Color(0xFF475569),
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp
            )
        )
    }
}
