package com.example.messqlpu.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.messqlpu.domain.model.CoolAvatar
import com.example.messqlpu.ui.theme.PrimaryPurple

@Composable
fun CoolAvatarView(
    avatar: CoolAvatar,
    size: Dp = 64.dp,
    showEditBadge: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Outer glowing ring
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(avatar.gradientBrush)
                .border(2.dp, Color.White.copy(alpha = 0.85f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Inner subtle gradient ring for 3D depth
            Box(
                modifier = Modifier
                    .fillMaxSize(0.92f)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                val emojiSize = (size.value * 0.48f).sp
                Text(
                    text = avatar.emoji,
                    fontSize = emojiSize,
                    lineHeight = emojiSize
                )
            }
        }

        // Optional Edit Pencil Badge
        if (showEditBadge) {
            val badgeSize = (size.value * 0.35f).coerceIn(20f, 28f).dp
            val iconSize = (badgeSize.value * 0.52f).dp
            Box(
                modifier = Modifier
                    .size(badgeSize)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(PrimaryPurple)
                    .border(2.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Avatar",
                    tint = Color.White,
                    modifier = Modifier.size(iconSize)
                )
            }
        }
    }
}
