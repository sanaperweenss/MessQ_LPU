package com.example.messqlpu.domain.model

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

data class CoolAvatar(
    val id: String,
    val name: String,
    val tag: String,
    val emoji: String,
    val bgStartColor: Color,
    val bgEndColor: Color,
    val description: String
) {
    val gradientBrush: Brush
        get() = Brush.linearGradient(listOf(bgStartColor, bgEndColor))
}

object CoolAvatarCatalog {
    val avatars = listOf(
        CoolAvatar(
            id = "avatar_scholar",
            name = "Cyber Scholar",
            tag = "Dean's Lister",
            emoji = "🎓",
            bgStartColor = Color(0xFF6C4CF1),
            bgEndColor = Color(0xFF9333EA),
            description = "High-GPA speed diner who calculates exact queue wait times."
        ),
        CoolAvatar(
            id = "avatar_foodie",
            name = "Slice Legend",
            tag = "Street Foodie",
            emoji = "🍕",
            bgStartColor = Color(0xFFF97316),
            bgEndColor = Color(0xFFEF4444),
            description = "Knows every butter naan & paneer recipe across Punjab."
        ),
        CoolAvatar(
            id = "avatar_robo",
            name = "AI Robo-Chef",
            tag = "Smart Predictor",
            emoji = "🤖",
            bgStartColor = Color(0xFF0EA5E9),
            bgEndColor = Color(0xFF3B82F6),
            description = "Forecasts dining rushes using campus crowd intelligence."
        ),
        CoolAvatar(
            id = "avatar_caffeine",
            name = "Caffeine Dynamo",
            tag = "Night Owl",
            emoji = "☕",
            bgStartColor = Color(0xFF8B5CF6),
            bgEndColor = Color(0xFFEC4899),
            description = "Runs on Cafe Express cold coffee and 2 AM hostel maggi."
        ),
        CoolAvatar(
            id = "avatar_ninja",
            name = "Ramen Ninja",
            tag = "Chopstick Pro",
            emoji = "🍜",
            bgStartColor = Color(0xFF10B981),
            bgEndColor = Color(0xFF047857),
            description = "Stealthily skips the crowd right when tokens open."
        ),
        CoolAvatar(
            id = "avatar_burger",
            name = "Burger Kingpin",
            tag = "Hostel Legend",
            emoji = "🍔",
            bgStartColor = Color(0xFFF59E0B),
            bgEndColor = Color(0xFFEA580C),
            description = "Can eat 4 aloo tikki burgers before a 9 AM lecture."
        ),
        CoolAvatar(
            id = "avatar_speed",
            name = "Speed Diner",
            tag = "Queue Runner",
            emoji = "⚡",
            bgStartColor = Color(0xFFEAB308),
            bgEndColor = Color(0xFFF97316),
            description = "In and out of the dining hall in 4 minutes flat."
        ),
        CoolAvatar(
            id = "avatar_vip",
            name = "Mess VIP",
            tag = "Campus Royalty",
            emoji = "👑",
            bgStartColor = Color(0xFFA855F7),
            bgEndColor = Color(0xFF6366F1),
            description = "Only dines when the crowd level is guaranteed low."
        )
    )

    fun getById(id: String): CoolAvatar =
        avatars.find { it.id == id } ?: avatars.first()
}
