package com.example.messqlpu.presentation.screens.cart

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.messqlpu.ui.theme.PrimaryPurple
import com.example.messqlpu.ui.theme.SuccessGreen

@Composable
fun OrderSuccessScreen(
    orderNumber: String = "#5482",
    pickupTime: String = "1:25 PM",
    onViewOrder: () -> Unit,
    onContinueExploring: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "success_scale")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Scaffold(
        containerColor = Color(0xFFF8F9FC)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(30.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Confetti & Green Check Badge
                Box(
                    modifier = Modifier.size(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Confetti dots canvas
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        // Top left yellow
                        drawCircle(Color(0xFFFBBF24), radius = 6f, center = Offset(w * 0.2f, h * 0.2f))
                        // Top right blue
                        drawCircle(Color(0xFF38BDF8), radius = 7f, center = Offset(w * 0.8f, h * 0.18f))
                        // Mid left orange
                        drawCircle(Color(0xFFFB923C), radius = 8f, center = Offset(w * 0.12f, h * 0.5f))
                        // Mid right green
                        drawCircle(SuccessGreen, radius = 6f, center = Offset(w * 0.88f, h * 0.45f))
                        // Bottom left purple
                        drawCircle(PrimaryPurple, radius = 7f, center = Offset(w * 0.22f, h * 0.82f))
                        // Bottom right coral
                        drawCircle(Color(0xFFF43F5E), radius = 6f, center = Offset(w * 0.78f, h * 0.78f))
                        // Extra festive dots
                        drawCircle(Color(0xFFA855F7), radius = 5f, center = Offset(w * 0.45f, h * 0.08f))
                        drawCircle(Color(0xFF10B981), radius = 5f, center = Offset(w * 0.58f, h * 0.92f))
                    }

                    // Green Success Circle with Check
                    Surface(
                        modifier = Modifier
                            .size(76.dp)
                            .scale(scale),
                        shape = CircleShape,
                        color = SuccessGreen,
                        shadowElevation = 8.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Order Confirmed",
                                tint = Color.White,
                                modifier = Modifier.size(44.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Order Confirmed!",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        fontSize = 24.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Your order has been placed successfully.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF64748B),
                        fontSize = 14.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Details Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp, horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Order Number",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF64748B),
                                fontSize = 13.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (orderNumber.startsWith("#")) orderNumber else "#5482",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                fontSize = 22.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = "Pickup Time",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF64748B),
                                fontSize = 13.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = pickupTime.ifEmpty { com.example.messqlpu.presentation.viewmodel.generateDynamicPickupSlots().firstOrNull() ?: "15 mins" },
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                fontSize = 18.sp
                            )
                        )
                    }
                }
            }

            // Bottom Buttons & Help
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Soft/Light Purple button matching mockup
                Button(
                    onClick = onViewOrder,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEDE9FE)
                    )
                ) {
                    Text(
                        text = "View Order Details",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = PrimaryPurple,
                            fontSize = 15.sp
                        )
                    )
                }

                // Help text
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Need help? ",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF64748B),
                            fontSize = 13.sp
                        )
                    )
                    Text(
                        text = "Contact Support",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = PrimaryPurple,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        ),
                        modifier = Modifier.clickable { onContinueExploring() }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}
