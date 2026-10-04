package com.example.messqlpu.data.repository

import com.example.messqlpu.data.MockDataProvider
import com.example.messqlpu.data.firebase.FirebaseManager
import com.example.messqlpu.data.local.MessQDatabase
import com.example.messqlpu.domain.model.*
import com.example.messqlpu.domain.repository.*
import kotlinx.coroutines.flow.*

class DiningRepositoryImpl(private val db: MessQDatabase) : IDiningRepository {
    private val hallsState = MutableStateFlow(MockDataProvider.sampleDiningHalls)

    init {
        db.dao.setDiningHalls(MockDataProvider.sampleDiningHalls)
    }

    override fun getDiningHalls(): Flow<List<DiningHall>> = hallsState.asStateFlow()

    override fun getDiningHall(id: String): Flow<DiningHall?> =
        hallsState.map { list -> list.find { it.id == id } }

    override fun searchDiningHalls(query: String, type: DiningType): Flow<List<DiningHall>> {
        return hallsState.map { list ->
            list.filter { hall ->
                val matchesType = type == DiningType.ALL || hall.type == type
                val matchesQuery = query.isBlank() || hall.name.contains(query, ignoreCase = true) ||
                        hall.description.contains(query, ignoreCase = true) ||
                        hall.facilities.any { it.contains(query, ignoreCase = true) }
                matchesType && matchesQuery
            }
        }
    }

    private val _reservationsFlow = MutableStateFlow<List<TableReservation>>(emptyList())
    override val reservationsFlow: StateFlow<List<TableReservation>> = _reservationsFlow.asStateFlow()

    override suspend fun updateCapacity(hallId: String, current: Int, crowd: CrowdLevel, waitMin: Int) {
        hallsState.update { list ->
            list.map { hall ->
                if (hall.id == hallId) {
                    hall.copy(capacityCurrent = current, crowdLevel = crowd, waitTimeMinutes = waitMin)
                } else hall
            }
        }
    }

    override suspend fun bookTable(reservation: TableReservation): TableReservation {
        _reservationsFlow.update { listOf(reservation) + it }
        FirebaseManager.emitNotification(
            title = "🪑 Table Reserved: ${reservation.diningHallName}",
            message = "Booking #${reservation.reservationId} confirmed for ${reservation.partySize} guests on ${reservation.date} at ${reservation.timeSlot}.",
            type = "TABLE_RESERVATION"
        )
        return reservation
    }

    override suspend fun cancelReservation(reservationId: String) {
        val target = _reservationsFlow.value.find { it.reservationId == reservationId }
        _reservationsFlow.update { list -> list.filter { it.reservationId != reservationId } }
        if (target != null) {
            FirebaseManager.emitNotification(
                title = "Table Reservation Cancelled",
                message = "Your table booking #${target.reservationId} for ${target.diningHallName} has been cancelled.",
                type = "TABLE_CANCELLED"
            )
        }
    }
}

class QueueRepositoryImpl(private val db: MessQDatabase) : IQueueRepository {
    // Initial active queue token matching Screen 7 requirement
    private val _activeTokenFlow = MutableStateFlow<QueueToken?>(QueueToken())
    override val activeTokenFlow: StateFlow<QueueToken?> = _activeTokenFlow.asStateFlow()

    override suspend fun joinQueue(
        diningHall: DiningHall,
        studentId: String,
        studentName: String
    ): QueueToken {
        val newTokenNumber = diningHall.estimatedToken
        val currentServed = (newTokenNumber - 23).coerceAtLeast(1)
        val newToken = QueueToken(
            tokenNumber = newTokenNumber,
            diningHallId = diningHall.id,
            diningHallName = diningHall.name,
            studentId = studentId,
            studentName = studentName,
            status = QueueStatus.WAITING,
            currentServingToken = currentServed,
            totalInBatch = newTokenNumber,
            remainingAhead = 23,
            estimatedWaitMinutes = diningHall.waitTimeMinutes,
            notifyMe = true,
            liveUpdates = listOf(
                "Queue joined successfully for ${diningHall.name}.",
                "Tokens $currentServed-${currentServed + 4} currently being called.",
                "Estimated call time in ~${diningHall.waitTimeMinutes} minutes."
            )
        )
        _activeTokenFlow.value = newToken
        db.dao.setActiveQueue(newToken)
        FirebaseManager.syncQueueToken(newToken)

        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.MINUTE, diningHall.waitTimeMinutes)
        val expectedCallTimeStr = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault()).format(cal.time)

        FirebaseManager.emitNotification(
            title = "Queue Joined: #${newToken.tokenNumber}",
            message = "In line at ${diningHall.name}. Est. wait: ${diningHall.waitTimeMinutes} mins (Expected call at $expectedCallTimeStr).",
            type = "QUEUE_JOIN"
        )
        return newToken
    }

    override suspend fun leaveQueue() {
        _activeTokenFlow.value = null
        db.dao.setActiveQueue(null)
    }

    override suspend fun toggleNotifications(enabled: Boolean) {
        _activeTokenFlow.update { it?.copy(notifyMe = enabled) }
    }

    override suspend fun adminAdvanceQueue(hallId: String) {
        var isNowCalling = false
        var currentTokenNum = 0
        var hallName = ""

        _activeTokenFlow.update { token ->
            if (token != null && token.diningHallId == hallId) {
                val nextServed = (token.currentServingToken + 1).coerceAtMost(token.tokenNumber)
                val remaining = (token.tokenNumber - nextServed).coerceAtLeast(0)
                val newWait = if (remaining == 0) 0 else ((remaining * 1.5).toInt()).coerceAtLeast(1)
                val newStatus = if (remaining == 0) QueueStatus.CALLING else QueueStatus.WAITING

                if (remaining == 0 && token.status != QueueStatus.CALLING) {
                    isNowCalling = true
                    currentTokenNum = token.tokenNumber
                    hallName = token.diningHallName
                }

                token.copy(
                    currentServingToken = nextServed,
                    remainingAhead = remaining,
                    estimatedWaitMinutes = newWait,
                    status = newStatus,
                    liveUpdates = listOf("Now serving token #$nextServed at Counter 2.") + token.liveUpdates
                )
            } else token
        }

        if (isNowCalling) {
            FirebaseManager.emitNotification(
                title = "🎉 Pickup Ready Earlier! Woohoo!",
                message = "Token #$currentTokenNum at $hallName is ready ahead of schedule! Proceed to Counter 2 immediately.",
                type = "QUEUE_CALLING"
            )
        }

        _activeTokenFlow.value?.let { token ->
            FirebaseManager.updateAdminQueueInFirestore(hallId, token.currentServingToken, token.tokenNumber)
        }
    }

    override suspend fun updateQueueDelay(hallId: String, waitMinutes: Int) {
        var notifyHallName: String? = null
        _activeTokenFlow.update { token ->
            if (token != null && token.diningHallId == hallId) {
                notifyHallName = token.diningHallName
                token.copy(
                    estimatedWaitMinutes = waitMinutes,
                    liveUpdates = listOf("⚠️ Delay Alert: Wait time updated to $waitMinutes mins.") + token.liveUpdates
                )
            } else token
        }
        notifyHallName?.let { hallName ->
            FirebaseManager.emitNotification(
                title = "⚠️ Queue Delay Alert: $hallName",
                message = "Heavy crowd detected at $hallName. Estimated wait is now $waitMinutes mins.",
                type = "QUEUE_DELAY"
            )
        }
    }
}

class MenuRepositoryImpl(private val db: MessQDatabase) : IMenuRepository {
    private val itemsState = MutableStateFlow(MockDataProvider.sampleFoodItems)

    init {
        db.dao.setFoodItems(MockDataProvider.sampleFoodItems)
    }

    override fun getFoodItemsForHall(hallId: String): Flow<List<FoodItem>> =
        itemsState.map { list ->
            if (hallId.isBlank()) {
                list
            } else {
                val matched = list.filter { it.diningHallId == hallId }
                if (matched.isNotEmpty()) matched else list
            }
        }

    override fun getAllFoodItems(): Flow<List<FoodItem>> = itemsState.asStateFlow()

    override suspend fun getFoodItemById(id: String): FoodItem? =
        itemsState.value.find { it.id == id }
}

class CartRepositoryImpl : ICartRepository {
    private val _cartFlow = MutableStateFlow(
        Cart(
            items = emptyList(),
            diningHallId = "dh_1",
            diningHallName = "Main Dining Hall",
            pickupTimeSlot = com.example.messqlpu.presentation.viewmodel.generateDynamicPickupSlots().firstOrNull() ?: "15 mins"
        )
    )
    override val cartFlow: StateFlow<Cart> = _cartFlow.asStateFlow()

    override fun addToCart(foodItem: FoodItem, addOns: List<FoodAddOn>, instructions: String) {
        _cartFlow.update { currentCart ->
            val existingIndex = currentCart.items.indexOfFirst { it.foodItem.id == foodItem.id }
            val updatedItems = if (existingIndex != -1) {
                currentCart.items.mapIndexed { index, item ->
                    if (index == existingIndex) {
                        item.copy(quantity = item.quantity + 1)
                    } else item
                }
            } else {
                currentCart.items + CartItem(
                    foodItem = foodItem,
                    quantity = 1,
                    selectedAddOns = addOns,
                    specialInstructions = instructions
                )
            }
            currentCart.copy(
                items = updatedItems,
                diningHallId = foodItem.diningHallId,
                diningHallName = foodItem.diningHallName
            )
        }
    }

    override fun updateQuantity(foodItemId: String, delta: Int) {
        _cartFlow.update { currentCart ->
            val updatedItems = currentCart.items.mapNotNull { item ->
                if (item.foodItem.id == foodItemId) {
                    val newQty = item.quantity + delta
                    if (newQty > 0) item.copy(quantity = newQty) else null
                } else item
            }
            currentCart.copy(items = updatedItems)
        }
    }

    override fun removeItem(foodItemId: String) {
        _cartFlow.update { currentCart ->
            currentCart.copy(items = currentCart.items.filter { it.foodItem.id != foodItemId })
        }
    }

    override fun clearCart() {
        _cartFlow.value = Cart()
    }

    override fun setPickupTime(timeSlot: String) {
        _cartFlow.update { it.copy(pickupTimeSlot = timeSlot) }
    }

    override fun setCookingNote(note: String) {
        _cartFlow.update { it.copy(cookingNote = note) }
    }
}

class OrderRepositoryImpl(private val db: MessQDatabase) : IOrderRepository {
    private val _ordersFlow = MutableStateFlow(MockDataProvider.sampleOrders)
    override val ordersFlow: StateFlow<List<Order>> = _ordersFlow.asStateFlow()

    override suspend fun placeOrder(cart: Cart, user: UserProfile): Order {
        val newOrderNumber = "MQ-" + (7000..9999).random()
        val order = Order(
            orderId = "ord_" + System.currentTimeMillis(),
            orderNumber = newOrderNumber,
            studentId = user.studentId,
            diningHallId = cart.diningHallId.ifEmpty { "dh_1" },
            diningHallName = cart.diningHallName.ifEmpty { "Main Dining Hall" },
            items = cart.items,
            subtotal = cart.subtotal,
            gstTax = cart.gstTax,
            platformFee = cart.platformFee,
            totalAmount = cart.total,
            status = OrderStatus.CONFIRMED,
            pickupTime = cart.pickupTimeSlot,
            orderDate = "Today, Just now",
            qrVerificationCode = "LPU-$newOrderNumber-CONFIRMED",
            tokenNumber = (100..299).random()
        )
        _ordersFlow.update { listOf(order) + it }
        db.dao.insertOrder(order)
        FirebaseManager.syncOrderToFirestore(order)
        FirebaseManager.emitNotification(
            title = "Order Placed: $newOrderNumber",
            message = "Your order at ${order.diningHallName} is confirmed for pickup at ${order.pickupTime}.",
            type = "ORDER_CONFIRMED"
        )
        return order
    }

    override suspend fun reorder(order: Order): Order {
        val dynamicPickupTime = com.example.messqlpu.presentation.viewmodel.generateDynamicPickupSlots().firstOrNull() ?: "In 15 mins"
        val reordered = order.copy(
            orderId = "ord_" + System.currentTimeMillis(),
            orderNumber = "MQ-" + (7000..9999).random(),
            pickupTime = dynamicPickupTime,
            orderDate = "Today, Just now",
            status = OrderStatus.CONFIRMED
        )
        _ordersFlow.update { listOf(reordered) + it }
        FirebaseManager.syncOrderToFirestore(reordered)
        FirebaseManager.emitNotification(
            title = "Order Reordered: ${reordered.orderNumber}",
            message = "Your order at ${reordered.diningHallName} is confirmed for pickup at ${reordered.pickupTime}.",
            type = "ORDER_CONFIRMED"
        )
        return reordered
    }

    override suspend fun rateOrder(orderId: String, rating: Int) {
        _ordersFlow.update { list ->
            list.map { if (it.orderId == orderId) it.copy(rating = rating) else it }
        }
    }
}

class AiAssistantRepositoryImpl : IAiAssistantRepository {
    override fun getRecommendations(): List<AiRecommendation> =
        MockDataProvider.sampleAiRecommendations

    override fun getInsightMessage(hallName: String): String =
        "$hallName expected to become crowded after 1:20 PM. Join now and save approximately 18 minutes."
}

class UserRepositoryImpl : IUserRepository {
    private val _userProfileFlow = MutableStateFlow(UserProfile())
    override val userProfileFlow: StateFlow<UserProfile> = _userProfileFlow.asStateFlow()

    private val _isDarkModeFlow = MutableStateFlow(false)
    override val isDarkModeFlow: StateFlow<Boolean> = _isDarkModeFlow.asStateFlow()

    override suspend fun login(id: String, pass: String, role: String): Result<UserProfile> {
        val result = FirebaseManager.signInWithEmail(id, pass)
        return if (result.isSuccess) {
            val profile = result.getOrThrow()
            _userProfileFlow.value = profile
            Result.success(profile)
        } else {
            Result.failure(result.exceptionOrNull() ?: Exception("Login failed"))
        }
    }

    override suspend fun signUp(name: String, email: String, pass: String, role: String): Result<UserProfile> {
        val cleanEmail = email.trim()
        val cleanName = name.trim().ifBlank { cleanEmail.substringBefore("@").replaceFirstChar { it.uppercase() } }
        val studentId = cleanEmail.substringBefore("@")
        val profile = UserProfile(
            studentId = studentId,
            name = cleanName,
            email = cleanEmail,
            role = role
        )
        val result = FirebaseManager.signUpWithEmail(cleanEmail, pass, profile)
        return if (result.isSuccess) {
            val finalProfile = result.getOrThrow()
            _userProfileFlow.value = finalProfile
            Result.success(finalProfile)
        } else {
            Result.failure(result.exceptionOrNull() ?: Exception("Sign up failed"))
        }
    }

    override suspend fun googleSignIn(context: android.content.Context): Result<UserProfile> {
        val webClientId = "626250518178-bbla9vnd4rj68tdvnb7q3t3tg47pp28q.apps.googleusercontent.com"
        return try {
            val credentialManager = androidx.credentials.CredentialManager.create(context)
            val googleIdOption = com.google.android.libraries.identity.googleid.GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = androidx.credentials.GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(context, request)
            val credential = result.credential
            if (credential is androidx.credentials.CustomCredential &&
                credential.type == com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                val authResult = FirebaseManager.signInWithGoogleToken(idToken)
                if (authResult.isSuccess) {
                    val profile = authResult.getOrThrow()
                    _userProfileFlow.value = profile
                    Result.success(profile)
                } else {
                    Result.failure(authResult.exceptionOrNull() ?: Exception("Firebase Google Auth failed."))
                }
            } else {
                Result.failure(Exception("Invalid credential response from Google."))
            }
        } catch (_: androidx.credentials.exceptions.GetCredentialCancellationException) {
            Result.failure(Exception("Google Sign-In was cancelled."))
        } catch (e: Throwable) {
            Result.failure(Exception(FirebaseManager.parseAuthErrorMessage(e)))
        }
    }

    override suspend fun updateDietaryPreference(pref: DietaryPreference) {
        _userProfileFlow.update { it.copy(dietaryPreference = pref) }
    }

    override suspend fun toggleDarkMode(enabled: Boolean) {
        _isDarkModeFlow.value = enabled
        _userProfileFlow.update { it.copy(darkModeEnabled = enabled) }
    }

    override suspend fun updateProfile(profile: UserProfile) {
        _userProfileFlow.value = profile
        FirebaseManager.updateCurrentUser(profile)
    }

    override suspend fun updateAvatar(avatarId: String) {
        _userProfileFlow.update { it.copy(avatarId = avatarId) }
        _userProfileFlow.value.let { FirebaseManager.updateCurrentUser(it) }
    }

    override suspend fun toggleFavoriteFood(foodId: String) {
        _userProfileFlow.update { current ->
            val updated = if (current.favoriteFoodIds.contains(foodId)) {
                current.favoriteFoodIds - foodId
            } else {
                current.favoriteFoodIds + foodId
            }
            current.copy(favoriteFoodIds = updated)
        }
        _userProfileFlow.value.let { FirebaseManager.updateCurrentUser(it) }
    }

    override suspend fun logout() {
        FirebaseManager.signOut()
    }
}

class AdminRepositoryImpl(
    private val queueRepo: IQueueRepository,
    private val diningRepo: IDiningRepository
) : IAdminRepository {
    private val _metricsFlow = MutableStateFlow(AdminDashboardMetrics())
    override val metricsFlow: StateFlow<AdminDashboardMetrics> = _metricsFlow.asStateFlow()

    override suspend fun advanceToken(hallId: String) {
        queueRepo.adminAdvanceQueue(hallId)
        _metricsFlow.update {
            it.copy(
                studentsWaiting = (it.studentsWaiting - 1).coerceAtLeast(0),
                avgServingSpeedSeconds = (35..45).random()
            )
        }
    }

    override suspend fun updateHallStatus(hallId: String, crowd: CrowdLevel, waitMinutes: Int) {
        diningRepo.updateCapacity(hallId, 250, crowd, waitMinutes)
        queueRepo.updateQueueDelay(hallId, waitMinutes)
    }
}
