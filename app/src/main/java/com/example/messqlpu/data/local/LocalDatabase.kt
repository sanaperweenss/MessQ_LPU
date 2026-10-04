package com.example.messqlpu.data.local

import com.example.messqlpu.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

/**
 * Room Schema Entities & DAO definitions
 */
data class DiningHallEntity(
    val id: String,
    val name: String,
    val type: String,
    val rating: Float,
    val reviewCount: Int,
    val distanceMeters: Int,
    val crowdLevel: String,
    val waitTimeMinutes: Int,
    val capacityTotal: Int,
    val capacityCurrent: Int,
    val imageUrl: String,
    val facilitiesJson: String,
    val openingHours: String,
    val latitude: Double,
    val longitude: Double,
    val description: String,
    val todaySpecial: String,
    val isRecommendedNow: Boolean
)

data class FoodItemEntity(
    val id: String,
    val diningHallId: String,
    val diningHallName: String,
    val name: String,
    val category: String,
    val price: Double,
    val rating: Float,
    val reviewCount: Int,
    val description: String,
    val imageUrl: String,
    val isVeg: Boolean,
    val calories: Int,
    val proteinG: Int,
    val fatG: Int,
    val carbsG: Int
)

data class QueueTokenEntity(
    val tokenNumber: Int,
    val diningHallId: String,
    val diningHallName: String,
    val studentId: String,
    val studentName: String,
    val status: String,
    val currentServingToken: Int,
    val remainingAhead: Int,
    val estimatedWaitMinutes: Int
)

/**
 * Room-compatible Reactive Data Access Object (DAO)
 */
class MessQDao {
    private val diningHallsFlow = MutableStateFlow<List<DiningHall>>(emptyList())
    private val foodItemsFlow = MutableStateFlow<List<FoodItem>>(emptyList())
    private val ordersFlow = MutableStateFlow<List<Order>>(emptyList())
    private val activeQueueFlow = MutableStateFlow<QueueToken?>(null)

    fun getAllDiningHalls(): Flow<List<DiningHall>> = diningHallsFlow.asStateFlow()

    fun getDiningHallById(id: String): Flow<DiningHall?> =
        diningHallsFlow.map { list -> list.find { it.id == id } }

    fun setDiningHalls(halls: List<DiningHall>) {
        diningHallsFlow.value = halls
    }

    fun getAllFoodItems(): Flow<List<FoodItem>> = foodItemsFlow.asStateFlow()

    fun getFoodItemsByDiningHall(hallId: String): Flow<List<FoodItem>> =
        foodItemsFlow.map { list -> list.filter { it.diningHallId == hallId } }

    fun setFoodItems(items: List<FoodItem>) {
        foodItemsFlow.value = items
    }

    fun getAllOrders(): Flow<List<Order>> = ordersFlow.asStateFlow()

    fun insertOrder(order: Order) {
        val updated = listOf(order) + ordersFlow.value.filter { it.orderId != order.orderId }
        ordersFlow.value = updated
    }

    fun setOrders(orders: List<Order>) {
        ordersFlow.value = orders
    }

    fun getActiveQueue(): Flow<QueueToken?> = activeQueueFlow.asStateFlow()

    fun setActiveQueue(token: QueueToken?) {
        activeQueueFlow.value = token
    }
}

/**
 * App Local Database singleton
 */
class MessQDatabase {
    val dao = MessQDao()

    companion object {
        @Volatile
        private var instance: MessQDatabase? = null

        fun getInstance(): MessQDatabase {
            return instance ?: synchronized(this) {
                instance ?: MessQDatabase().also { instance = it }
            }
        }
    }
}
