package com.example.messqlpu.presentation.screens.queue

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.messqlpu.presentation.viewmodel.QueueViewModel
import com.example.messqlpu.ui.theme.PrimaryPurple
import com.example.messqlpu.ui.theme.SuccessGreen
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun QueueProgressScreen(
    onBackClick: () -> Unit,
    viewModel: QueueViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val activeToken by viewModel.activeToken.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
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
                    text = "Queue Progress",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        fontSize = 20.sp
                    )
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // TOP TOKEN INFO CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = PrimaryPurple
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Restaurant,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "Token #${activeToken?.tokenNumber ?: "147"}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                fontSize = 19.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = activeToken?.diningHallName ?: "Main Dining Hall",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color(0xFF64748B),
                                fontSize = 14.sp
                            )
                        )
                    }
                }
            }

            // SECTION TITLE: Queue Timeline
            Text(
                text = "Queue Timeline",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    fontSize = 17.sp
                )
            )

            val cal = Calendar.getInstance()
            val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
            val currentWait = activeToken?.estimatedWaitMinutes ?: 8
            val isCalling = (activeToken?.remainingAhead ?: 0) == 0 || activeToken?.status == com.example.messqlpu.domain.model.QueueStatus.CALLING

            val joinedCal = cal.clone() as Calendar
            joinedCal.add(Calendar.MINUTE, -10)
            val joinedTimeStr = timeFormat.format(joinedCal.time)

            val expectedCal = cal.clone() as Calendar
            expectedCal.add(Calendar.MINUTE, currentWait)
            val expectedCallTimeStr = if (isCalling) "Now Calling!" else timeFormat.format(expectedCal.time)

            // TIMELINE CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Node 1: Joined Queue
                    TimelineRow(
                        isCompleted = true,
                        isCurrent = false,
                        isLast = false,
                        title = "Joined Queue",
                        subtitle = joinedTimeStr,
                        subtitleColor = Color(0xFF64748B),
                        nodeType = NodeType.CHECK
                    )

                    // Node 2: Token Currently Being Served
                    val servingNum = activeToken?.currentServingToken?.coerceAtMost(activeToken?.tokenNumber ?: 147) ?: 124
                    TimelineRow(
                        isCompleted = true,
                        isCurrent = false,
                        isLast = false,
                        title = "Token #$servingNum",
                        subtitle = "Currently being served",
                        subtitleColor = SuccessGreen,
                        nodeType = NodeType.CHECK
                    )

                    // Node 3: Your Token
                    TimelineRow(
                        isCompleted = isCalling,
                        isCurrent = !isCalling,
                        isLast = false,
                        title = "Your Token #${activeToken?.tokenNumber ?: "147"}",
                        subtitle = if (isCalling) "🎉 Ready for Pickup!" else "Est. $currentWait minutes",
                        subtitleColor = if (isCalling) SuccessGreen else PrimaryPurple,
                        nodeType = if (isCalling) NodeType.CHECK else NodeType.PURPLE_CURRENT
                    )

                    // Node 4: Expected Call
                    TimelineRow(
                        isCompleted = isCalling,
                        isCurrent = false,
                        isLast = true,
                        title = if (isCalling) "Called at Counter 2" else "Expected Call",
                        subtitle = expectedCallTimeStr,
                        subtitleColor = if (isCalling) SuccessGreen else Color(0xFF64748B),
                        nodeType = if (isCalling) NodeType.CHECK else NodeType.FUTURE
                    )
                }
            }

            // AI PREDICTION CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F0FF)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(
                        modifier = Modifier.size(36.dp),
                        shape = CircleShape,
                        color = PrimaryPurple.copy(alpha = 0.15f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = PrimaryPurple,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "AI Prediction",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                fontSize = 15.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Slight delay expected (~3 mins) due to higher than usual crowd.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color(0xFF475569),
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

enum class NodeType {
    CHECK,
    PURPLE_CURRENT,
    FUTURE
}

@Composable
private fun TimelineRow(
    isCompleted: Boolean,
    isCurrent: Boolean,
    isLast: Boolean,
    title: String,
    subtitle: String,
    subtitleColor: Color,
    nodeType: NodeType
) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (nodeType) {
                NodeType.CHECK -> {
                    Surface(
                        modifier = Modifier.size(28.dp),
                        shape = CircleShape,
                        color = SuccessGreen
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                NodeType.PURPLE_CURRENT -> {
                    Surface(
                        modifier = Modifier.size(28.dp),
                        shape = CircleShape,
                        color = PrimaryPurple.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(2.dp, PrimaryPurple)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryPurple)
                            )
                        }
                    }
                }
                NodeType.FUTURE -> {
                    Surface(
                        modifier = Modifier.size(28.dp),
                        shape = CircleShape,
                        color = Color.Transparent,
                        border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFCBD5E1))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(46.dp)
                        .background(
                            if (isCompleted) SuccessGreen else Color(0xFFE2E8F0)
                        )
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (!isLast) 28.dp else 4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isCurrent) PrimaryPurple else Color(0xFF0F172A),
                    fontSize = 15.sp
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = subtitleColor,
                    fontWeight = if (isCurrent || subtitleColor == SuccessGreen) FontWeight.SemiBold else FontWeight.Normal,
                    fontSize = 13.sp
                )
            )
        }
    }
}
