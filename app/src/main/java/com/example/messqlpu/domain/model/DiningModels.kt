package com.example.messqlpu.domain.model

import androidx.compose.ui.graphics.Color
import com.example.messqlpu.ui.theme.CrowdHigh
import com.example.messqlpu.ui.theme.CrowdLow
import com.example.messqlpu.ui.theme.CrowdModerate
import com.example.messqlpu.ui.theme.CrowdSevere

enum class CrowdLevel(val label: String, val badgeColor: Color) {
    LOW("Low Crowd", CrowdLow),
    MODERATE("Moderate Crowd", CrowdModerate),
    HIGH("High Crowd", CrowdHigh),
    SEVERE("Heavy Crowd", CrowdSevere)
}

enum class DiningType(val displayName: String) {
    ALL("All"),
    MESS("Mess"),
    CAFES("Cafes"),
    FOOD_COURT("Food Court")
}

data class DiningHall(
    val id: String,
    val name: String,
    val type: DiningType,
    val rating: Float,
    val reviewCount: Int,
    val distanceMeters: Int,
    val crowdLevel: CrowdLevel,
    val waitTimeMinutes: Int,
    val capacityTotal: Int,
    val capacityCurrent: Int,
    val imageUrl: String,
    val facilities: List<String>,
    val openingHours: String,
    val latitude: Double,
    val longitude: Double,
    val description: String,
    val todaySpecial: String = "",
    val isRecommendedNow: Boolean = false,
    val estimatedToken: Int = 148
) {
    val distanceDisplay: String
        get() = if (distanceMeters >= 1000) {
            "%.1f km".format(distanceMeters / 1000f)
        } else {
            "$distanceMeters m"
        }

    val occupancyPercent: Int
        get() = if (capacityTotal > 0) ((capacityCurrent.toFloat() / capacityTotal) * 100).toInt().coerceIn(0, 100) else 0
}
