package com.example.messqlpu.presentation.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.messqlpu.presentation.components.MessQTopBar
import com.example.messqlpu.presentation.components.ModernCard
import com.example.messqlpu.ui.theme.PrimaryPurple
import com.example.messqlpu.ui.theme.SuccessGreen
import com.example.messqlpu.ui.theme.WarningAmber

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val time: String,
    val type: String
)

@Composable
fun NotificationCenterScreen(
    onBackClick: () -> Unit
) {
    val notifications by com.example.messqlpu.data.firebase.FirebaseManager.notificationsListFlow.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            MessQTopBar(
                title = "Notification Center",
                onBackClick = onBackClick
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp)
        ) {
            items(notifications) { item ->
                ModernCard {
                    Row(verticalAlignment = Alignment.Top) {
                        Surface(
                            modifier = Modifier.size(42.dp),
                            shape = CircleShape,
                            color = when (item.type) {
                                "QUEUE" -> PrimaryPurple.copy(alpha = 0.12f)
                                "ORDER" -> SuccessGreen.copy(alpha = 0.12f)
                                "CROWD" -> WarningAmber.copy(alpha = 0.12f)
                                else -> PrimaryPurple.copy(alpha = 0.12f)
                            }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = when (item.type) {
                                        "QUEUE" -> Icons.Default.ConfirmationNumber
                                        "ORDER" -> Icons.Default.Fastfood
                                        "CROWD" -> Icons.Default.Groups
                                        else -> Icons.Default.Notifications
                                    },
                                    contentDescription = null,
                                    tint = when (item.type) {
                                        "QUEUE" -> PrimaryPurple
                                        "ORDER" -> SuccessGreen
                                        "CROWD" -> WarningAmber
                                        else -> PrimaryPurple
                                    },
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.weight(1f),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = item.time,
                                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                                    maxLines = 1
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.message,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
