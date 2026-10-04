package com.example.messqlpu.presentation.screens.queue

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.messqlpu.presentation.components.EmptyQueueStateView
import com.example.messqlpu.presentation.viewmodel.QueueViewModel
import com.example.messqlpu.ui.theme.DangerRed
import com.example.messqlpu.ui.theme.PrimaryPurple
import com.example.messqlpu.ui.theme.PrimaryPurpleBg
import com.example.messqlpu.ui.theme.SuccessGreen
import com.example.messqlpu.ui.theme.WarningAmber

@Composable
fun LiveQueueScreen(
    onNavigateToExplore: () -> Unit,
    onNavigateToProgressTimeline: () -> Unit,
    viewModel: QueueViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val activeToken by viewModel.activeToken.collectAsState()
    val context = LocalContext.current
    var showLeaveDialog by remember { mutableStateOf(false) }

    if (activeToken == null) {
        EmptyQueueStateView(onExploreClick = onNavigateToExplore)
        return
    }

    val token = activeToken!!
    val isCalling = token.remainingAhead == 0 || token.status == com.example.messqlpu.domain.model.QueueStatus.CALLING
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(isCalling) {
        if (isCalling) {
            snackbarHostState.showSnackbar(
                message = "🎉 Pickup was delivered earlier! Woohoo! Token #${token.tokenNumber} is Ready for Pickup at Counter 2."
            )
        }
    }

    if (showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveDialog = false },
            title = { Text("Leave Queue?") },
            text = { Text("If you leave now, you will lose your token #${token.tokenNumber} and need to rejoin from the back.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.leaveQueue()
                        showLeaveDialog = false
                    }
                ) {
                    Text("Leave Queue", color = DangerRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveDialog = false }) {
                    Text("Stay in Line")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                    Text(
                        text = "My Queue",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    FilledTonalButton(
                        onClick = onNavigateToProgressTimeline,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = PrimaryPurpleBg)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timeline,
                            contentDescription = "Timeline",
                            tint = PrimaryPurple,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Timeline",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = PrimaryPurple,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // HERO PURPLE GRADIENT PASS CARD (Exact design from Image 1 / Screen 7)
            // HERO PASS CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                colors = if (isCalling) {
                                    listOf(Color(0xFF15803D), Color(0xFF10B981))
                                } else {
                                    listOf(Color(0xFF5B3DE8), Color(0xFF8B5CF6))
                                }
                            )
                        )
                        .padding(22.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isCalling) "🎉 PICKUP READY EARLIER! WOOHOO!" else "Your Token",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White.copy(alpha = 0.95f),
                                fontWeight = FontWeight.Bold
                            )
                        )

                        Text(
                            text = "#${token.tokenNumber}",
                            style = MaterialTheme.typography.displayLarge.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 48.sp
                            )
                        )

                        Text(
                            text = token.diningHallName,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White.copy(alpha = 0.85f),
                                fontWeight = FontWeight.Medium
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Silhouette icons of students at counter
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(5) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = if (isCalling) Color.White.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.4f),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }

            // QUEUE STATUS CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val displayServed = token.currentServingToken.coerceAtMost(token.tokenNumber)
                    Text(
                        text = "$displayServed / ${token.tokenNumber} Served",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = if (isCalling) "No students ahead — It's your turn!" else "${token.remainingAhead} students ahead",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (isCalling) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isCalling) FontWeight.Bold else FontWeight.Normal
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val progress = if (token.tokenNumber > 0) (displayServed.toFloat() / token.tokenNumber.toFloat()).coerceIn(0f, 1f) else 0f
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (isCalling) SuccessGreen else PrimaryPurple,
                        trackColor = PrimaryPurpleBg
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = if (isCalling) "Status" else "Estimated Wait",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Text(
                        text = if (isCalling) "Ready Now!" else "${token.estimatedWaitMinutes} Minutes",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isCalling) SuccessGreen else MaterialTheme.colorScheme.onSurface
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Notification / Ready Banner
                    if (isCalling) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SuccessGreen.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(text = "🎉", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "🎉 PICKUP WAS DELIVERED EARLIER! WOOHOO! Proceed to Counter 2",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = SuccessGreen,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                )
                            }
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = WarningAmber.copy(alpha = 0.12f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(text = "🔔", fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "We'll notify you when it's your turn!",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = WarningAmber,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Buttons: [ 🔔 Notify Me ]  [ 🔗 Share Token ]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.toggleNotification(!token.notifyMe) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryPurple)
                        ) {
                            Icon(
                                imageVector = if (token.notifyMe) Icons.Default.NotificationsActive else Icons.Default.NotificationsNone,
                                contentDescription = null,
                                tint = PrimaryPurple,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (token.notifyMe) "Notify ON" else "Notify Me",
                                color = PrimaryPurple,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, "I have token #${token.tokenNumber} at ${token.diningHallName}! Current # ${token.currentServingToken} on MessQ LPU.")
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share Token"))
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Share Token",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Leave Queue text button
                    TextButton(onClick = { showLeaveDialog = true }) {
                        Text(
                            text = "Leave Queue",
                            color = DangerRed,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            // LIVE UPDATES
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Live Updates",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    val liveLogs = if (token.liveUpdates.isNotEmpty()) {
                        token.liveUpdates.take(5)
                    } else {
                        listOf(
                            "Now serving token #${token.currentServingToken} at Counter 2.",
                            "Queue joined for ${token.diningHallName}.",
                            "Estimated call time in ~${token.estimatedWaitMinutes} minutes."
                        )
                    }

                    liveLogs.forEachIndexed { index, updateText ->
                        val timeAgo = when (index) {
                            0 -> "Just now"
                            1 -> "1 min ago"
                            2 -> "3 mins ago"
                            else -> "${(index + 1) * 2} mins ago"
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(if (index == 0) SuccessGreen else PrimaryPurple)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = updateText,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = timeAgo,
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }
                }
            }

            // BOTTOM AI PREDICTION CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryPurple.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = null,
                        tint = PrimaryPurple,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "AI Prediction",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = PrimaryPurple
                            )
                        )
                        Text(
                            text = "Slight delay expected (~3 mins) due to higher than usual crowd.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
