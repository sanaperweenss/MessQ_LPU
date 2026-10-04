package com.example.messqlpu.domain.repository

import com.example.messqlpu.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface IDiningRepository {
    val reservationsFlow: StateFlow<List<TableReservation>>
    fun getDiningHalls(): Flow<List<DiningHall>>
    fun getDiningHall(id: String): Flow<DiningHall?>
    fun searchDiningHalls(query: String, type: DiningType): Flow<List<DiningHall>>
    suspend fun updateCapacity(hallId: String, current: Int, crowd: CrowdLevel, waitMin: Int)
    suspend fun bookTable(reservation: TableReservation): TableReservation
    suspend fun cancelReservation(reservationId: String)
}

interface IQueueRepository {
    val activeTokenFlow: StateFlow<QueueToken?>
    suspend fun joinQueue(diningHall: DiningHall, studentId: String, studentName: String): QueueToken
    suspend fun leaveQueue()
    suspend fun toggleNotifications(enabled: Boolean)
    suspend fun adminAdvanceQueue(hallId: String)
    suspend fun updateQueueDelay(hallId: String, waitMinutes: Int)
}

interface IMenuRepository {
    fun getFoodItemsForHall(hallId: String): Flow<List<FoodItem>>
    fun getAllFoodItems(): Flow<List<FoodItem>>
    suspend fun getFoodItemById(id: String): FoodItem?
}

interface ICartRepository {
    val cartFlow: StateFlow<Cart>
    fun addToCart(foodItem: FoodItem, addOns: List<FoodAddOn> = emptyList(), instructions: String = "")
    fun updateQuantity(foodItemId: String, delta: Int)
    fun removeItem(foodItemId: String)
    fun clearCart()
    fun setPickupTime(timeSlot: String)
    fun setCookingNote(note: String)
}

interface IOrderRepository {
    val ordersFlow: StateFlow<List<Order>>
    suspend fun placeOrder(cart: Cart, user: UserProfile): Order
    suspend fun reorder(order: Order): Order
    suspend fun rateOrder(orderId: String, rating: Int)
}

interface IAiAssistantRepository {
    fun getRecommendations(): List<AiRecommendation>
    fun getInsightMessage(hallName: String): String
}

interface IUserRepository {
    val userProfileFlow: StateFlow<UserProfile>
    val isDarkModeFlow: StateFlow<Boolean>
    suspend fun login(id: String, pass: String, role: String): Result<UserProfile>
    suspend fun signUp(name: String, email: String, pass: String, role: String): Result<UserProfile>
    suspend fun googleSignIn(context: android.content.Context): Result<UserProfile>
    suspend fun updateDietaryPreference(pref: DietaryPreference)
    suspend fun toggleDarkMode(enabled: Boolean)
    suspend fun updateProfile(profile: UserProfile)
    suspend fun updateAvatar(avatarId: String)
    suspend fun toggleFavoriteFood(foodId: String)
    suspend fun logout()
}

interface IAdminRepository {
    val metricsFlow: StateFlow<AdminDashboardMetrics>
    suspend fun advanceToken(hallId: String)
    suspend fun updateHallStatus(hallId: String, crowd: CrowdLevel, waitMinutes: Int)
}
