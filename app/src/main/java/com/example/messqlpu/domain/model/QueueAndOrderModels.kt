package com.example.messqlpu.domain.model

enum class QueueStatus(val displayName: String) {
    WAITING("In Queue"),
    CALLING("Now Calling!"),
    SERVED("Served"),
    CANCELLED("Left Queue")
}

data class QueueTimelineStep(
    val id: String,
    val title: String,
    val time: String,
    val isDone: Boolean,
    val isCurrent: Boolean,
    val description: String
)

data class QueueToken(
    val tokenNumber: Int = 147,
    val diningHallId: String = "dh_1",
    val diningHallName: String = "Main Dining Hall",
    val studentId: String = "12108934",
    val studentName: String = "Aarav Sharma",
    val status: QueueStatus = QueueStatus.WAITING,
    val currentServingToken: Int = 124,
    val totalInBatch: Int = 147,
    val remainingAhead: Int = 23,
    val estimatedWaitMinutes: Int = 8,
    val notifyMe: Boolean = true,
    val liveUpdates: List<String> = listOf(
        "Counters 1, 2 & 4 are dispensing meals rapidly.",
        "Tokens 120-124 called to pickup window.",
        "Expected call for #147 in ~8 minutes."
    ),
    val timeline: List<QueueTimelineStep> = listOf(
        QueueTimelineStep("1", "Joined Queue", "12:32 PM", isDone = true, isCurrent = false, "Token #147 allocated"),
        QueueTimelineStep("2", "Current Token Being Served", "12:38 PM", isDone = true, isCurrent = false, "Serving token #124"),
        QueueTimelineStep("3", "User Token In Range", "12:44 PM", isDone = false, isCurrent = true, "Expected in ~6 mins"),
        QueueTimelineStep("4", "Meal Dispense Call", "12:46 PM", isDone = false, isCurrent = false, "Collect with digital token")
    ),
    val aiDelayProbability: String = "Low (12%)",
    val aiDelayNotice: String = "Kitchen velocity is optimal. No delays predicted for South/North counters."
)

enum class OrderStatus(val displayName: String) {
    CONFIRMED("Confirmed"),
    PREPARING("Preparing"),
    READY_FOR_PICKUP("Ready for Pickup"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled")
}

data class Order(
    val orderId: String,
    val orderNumber: String,
    val studentId: String,
    val diningHallId: String,
    val diningHallName: String,
    val items: List<CartItem>,
    val subtotal: Double,
    val gstTax: Double,
    val platformFee: Double,
    val totalAmount: Double,
    val status: OrderStatus,
    val pickupTime: String,
    val orderDate: String,
    val qrVerificationCode: String,
    val tokenNumber: Int,
    val rating: Int? = null
)

data class AiRecommendation(
    val diningHallId: String,
    val diningHallName: String,
    val title: String,
    val reasons: List<String>,
    val savedMinutes: Int,
    val crowdLevel: CrowdLevel,
    val waitMinutes: Int,
    val favoriteDishes: List<String>,
    val bestTimeToVisit: String
)

data class Review(
    val id: String,
    val authorName: String,
    val rating: Float,
    val date: String,
    val comment: String
)

enum class DietaryPreference(val label: String) {
    VEG("Vegetarian"),
    NON_VEG("Non-Vegetarian"),
    JAIN("Jain Food"),
    ALL("All Options")
}

data class UserProfile(
    val studentId: String = "12108934",
    val name: String = "Aarav Sharma",
    val email: String = "aarav.sharma@lpu.in",
    val role: String = "Student",
    val department: String = "School of Computer Science & Eng.",
    val hostel: String = "BH-4, Block B",
    val dietaryPreference: DietaryPreference = DietaryPreference.VEG,
    val notificationsEnabled: Boolean = true,
    val darkModeEnabled: Boolean = false,
    val locationAccessEnabled: Boolean = true,
    val avatarId: String = "avatar_scholar",
    val favoriteFoodIds: List<String> = listOf("f_1", "f_2")
)

data class AdminDashboardMetrics(
    val activeQueues: Int = 14,
    val studentsWaiting: Int = 186,
    val ordersToday: Int = 842,
    val diningHallCapacityPercent: Int = 68,
    val peakHoursAlert: String = "1:00 PM - 2:15 PM Peak Lunch Rush",
    val mostOrderedDish: String = "Special Rajma Rice Bowl",
    val avgServingSpeedSeconds: Int = 42
)

data class TableReservation(
    val reservationId: String = "RES-" + (1000..9999).random(),
    val diningHallId: String,
    val diningHallName: String,
    val studentName: String,
    val date: String,
    val timeSlot: String,
    val partySize: Int = 2,
    val seatingPreference: String = "AC Indoor",
    val timestamp: String = "Today, Just now"
)
