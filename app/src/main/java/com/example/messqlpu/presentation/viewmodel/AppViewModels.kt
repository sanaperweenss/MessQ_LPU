package com.example.messqlpu.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.messqlpu.MessQApplication
import com.example.messqlpu.domain.model.*
import com.example.messqlpu.domain.repository.*
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AuthViewModel(
    private val userRepo: IUserRepository = MessQApplication.instance.container.userRepository
) : ViewModel() {
    var emailOrId = MutableStateFlow("")
    var fullName = MutableStateFlow("")
    var password = MutableStateFlow("")
    var isSignUp = MutableStateFlow(false)
    var selectedRole = MutableStateFlow("Student")
    var isLoading = MutableStateFlow(false)
    var errorMessage = MutableStateFlow<String?>(null)

    fun login(onSuccess: () -> Unit) {
        val input = emailOrId.value.trim()
        val pass = password.value.trim()
        if (input.isBlank()) {
            errorMessage.value = "Please enter your email or Student ID"
            return
        }
        if (pass.isBlank()) {
            errorMessage.value = "Please enter your password"
            return
        }
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            val result = userRepo.login(input, pass, selectedRole.value)
            isLoading.value = false
            if (result.isSuccess) {
                onSuccess()
            } else {
                errorMessage.value = result.exceptionOrNull()?.message 
                    ?: "Login failed. Please check your credentials."
            }
        }
    }

    fun signUp(onSuccess: () -> Unit) {
        val name = fullName.value.trim()
        val email = emailOrId.value.trim()
        val pass = password.value.trim()
        if (name.isBlank()) {
            errorMessage.value = "Please enter your full name"
            return
        }
        if (email.isBlank() || !email.contains("@")) {
            errorMessage.value = "Please enter a valid email address (e.g. name@example.com)"
            return
        }
        if (pass.length < 6) {
            errorMessage.value = "Password must be at least 6 characters"
            return
        }
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            val result = userRepo.signUp(name, email, pass, selectedRole.value)
            isLoading.value = false
            if (result.isSuccess) {
                onSuccess()
            } else {
                errorMessage.value = result.exceptionOrNull()?.message 
                    ?: "Sign up failed. Please try again."
            }
        }
    }

    fun quickDemoLogin(onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            val demoProfile = UserProfile(
                studentId = "12108934",
                name = "Demo Student",
                email = "demo.student@lpu.in",
                role = "Student"
            )
            userRepo.updateProfile(demoProfile)
            isLoading.value = false
            onSuccess()
        }
    }

    fun googleSignIn(context: android.content.Context, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            val result = userRepo.googleSignIn(context)
            isLoading.value = false
            if (result.isSuccess) {
                onSuccess()
            } else {
                errorMessage.value = result.exceptionOrNull()?.message 
                    ?: "Google Sign-In failed. Please try again."
            }
        }
    }
}

data class HomeHeaderState(
    val greetingPrefix: String = "Good Day",
    val formattedDateTime: String = "",
    val diningPhase: String = "Regular dining hours",
    val tempText: String = "26°C",
    val weatherDesc: String = "Pleasant day",
    val weatherEmoji: String = "🌤️"
)

private data class HeaderInfo(
    val phase: String,
    val temp: String,
    val weather: String,
    val emoji: String
)

class HomeViewModel(
    private val diningRepo: IDiningRepository = MessQApplication.instance.container.diningRepository,
    private val menuRepo: IMenuRepository = MessQApplication.instance.container.menuRepository,
    private val queueRepo: IQueueRepository = MessQApplication.instance.container.queueRepository,
    private val aiRepo: IAiAssistantRepository = MessQApplication.instance.container.aiAssistantRepository,
    private val userRepo: IUserRepository = MessQApplication.instance.container.userRepository
) : ViewModel() {

    val userProfile: StateFlow<UserProfile> = userRepo.userProfileFlow

    val diningHalls: StateFlow<List<DiningHall>> = diningRepo.getDiningHalls()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayMenu: StateFlow<List<FoodItem>> = menuRepo.getAllFoodItems()
        .map { list -> list.take(4) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recommendedMeals: StateFlow<List<FoodItem>> = menuRepo.getAllFoodItems()
        .map { list -> list.shuffled().take(3) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val heroDiningHall: StateFlow<DiningHall?> = diningRepo.getDiningHalls()
        .map { list -> list.find { it.isRecommendedNow } ?: list.firstOrNull() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val homeHeaderState: StateFlow<HomeHeaderState> = userProfile
        .map { calculateHomeHeaderState() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), calculateHomeHeaderState())

    private fun calculateHomeHeaderState(): HomeHeaderState {
        val cal = Calendar.getInstance()
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val dayFormat = SimpleDateFormat("EEEE, h:mm a", Locale.getDefault())
        val dateTimeStr = dayFormat.format(cal.time)

        val greeting = when (hour) {
            in 5..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            in 17..21 -> "Good Evening"
            else -> "Good Night"
        }

        val info = when (hour) {
            in 6..10 -> HeaderInfo("Breakfast hours", "22°C", "Cool morning", "🌤️")
            in 11..15 -> HeaderInfo("Peak dining hour", "30°C", "Sunny day", "☀️")
            in 16..18 -> HeaderInfo("Snack time", "26°C", "Pleasant evening", "⛅")
            in 19..22 -> HeaderInfo("Peak dining hour", "23°C", "Clear night", "🌙")
            else -> HeaderInfo("Late night hours", "19°C", "Cool night", "🌌")
        }

        return HomeHeaderState(
            greetingPrefix = greeting,
            formattedDateTime = dateTimeStr,
            diningPhase = info.phase,
            tempText = info.temp,
            weatherDesc = info.weather,
            weatherEmoji = info.emoji
        )
    }

    val greetingText: String
        get() {
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            return when (hour) {
                in 5..11 -> "Good Morning, Student"
                in 12..16 -> "Good Afternoon, Student"
                in 17..21 -> "Good Evening, Student"
                else -> "Welcome, Night Owl"
            }
        }

    val aiInsightText: String =
        "Main Mess expected to become crowded after 1:20 PM. Join now and save approximately 18 minutes."

    fun joinHeroQueue(onJoined: () -> Unit) {
        val hall = heroDiningHall.value ?: return
        viewModelScope.launch {
            val user = userProfile.value
            queueRepo.joinQueue(hall, user.studentId, user.name)
            onJoined()
        }
    }
}

class ExploreViewModel(
    private val diningRepo: IDiningRepository = MessQApplication.instance.container.diningRepository
) : ViewModel() {
    val searchQuery = MutableStateFlow("")
    val selectedType = MutableStateFlow(DiningType.ALL)

    val diningHalls: StateFlow<List<DiningHall>> = combine(
        searchQuery,
        selectedType
    ) { query, type ->
        Pair(query, type)
    }.flatMapLatest { (query, type) ->
        diningRepo.searchDiningHalls(query, type)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchChanged(newQuery: String) {
        searchQuery.value = newQuery
    }

    fun onTypeSelected(type: DiningType) {
        selectedType.value = type
    }
}

class DiningDetailViewModel(
    private val hallId: String,
    private val diningRepo: IDiningRepository = MessQApplication.instance.container.diningRepository,
    private val menuRepo: IMenuRepository = MessQApplication.instance.container.menuRepository,
    private val queueRepo: IQueueRepository = MessQApplication.instance.container.queueRepository,
    private val userRepo: IUserRepository = MessQApplication.instance.container.userRepository
) : ViewModel() {

    val selectedTab = MutableStateFlow(0) // 0: Overview, 1: Menu, 2: Reviews, 3: Live Stats

    val diningHall: StateFlow<DiningHall?> = diningRepo.getDiningHall(hallId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val menuItems: StateFlow<List<FoodItem>> = menuRepo.getFoodItemsForHall(hallId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reviews = com.example.messqlpu.data.MockDataProvider.sampleReviews

    fun setTab(index: Int) {
        selectedTab.value = index
    }

    fun joinQueue(onJoined: () -> Unit) {
        val hall = diningHall.value ?: return
        viewModelScope.launch {
            val user = userRepo.userProfileFlow.value
            queueRepo.joinQueue(hall, user.studentId, user.name)
            onJoined()
        }
    }

    fun bookTable(
        date: String,
        timeSlot: String,
        partySize: Int,
        seating: String,
        onBooked: (TableReservation) -> Unit
    ) {
        val hall = diningHall.value ?: return
        val user = userRepo.userProfileFlow.value
        viewModelScope.launch {
            val reservation = TableReservation(
                diningHallId = hall.id,
                diningHallName = hall.name,
                studentName = user.name,
                date = date,
                timeSlot = timeSlot,
                partySize = partySize,
                seatingPreference = seating
            )
            val result = diningRepo.bookTable(reservation)
            onBooked(result)
        }
    }
}

class QueueViewModel(
    private val queueRepo: IQueueRepository = MessQApplication.instance.container.queueRepository
) : ViewModel() {
    val activeToken: StateFlow<QueueToken?> = queueRepo.activeTokenFlow

    fun toggleNotification(enabled: Boolean) {
        viewModelScope.launch {
            queueRepo.toggleNotifications(enabled)
        }
    }

    fun leaveQueue() {
        viewModelScope.launch {
            queueRepo.leaveQueue()
        }
    }
}

class MenuViewModel(
    private val hallId: String,
    private val menuRepo: IMenuRepository = MessQApplication.instance.container.menuRepository,
    private val cartRepo: ICartRepository = MessQApplication.instance.container.cartRepository,
    private val userRepo: IUserRepository = MessQApplication.instance.container.userRepository
) : ViewModel() {
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow<FoodCategory?>(null)
    val showFavoritesOnly = MutableStateFlow(false)

    val cart: StateFlow<Cart> = cartRepo.cartFlow

    val foodItems: StateFlow<List<FoodItem>> = combine(
        searchQuery,
        selectedCategory,
        showFavoritesOnly,
        userRepo.userProfileFlow
    ) { query, cat, favOnly, profile ->
        listOf(query, cat, favOnly, profile.favoriteFoodIds)
    }.flatMapLatest { params ->
        val query = params[0] as String
        val cat = params[1] as FoodCategory?
        val favOnly = params[2] as Boolean
        @Suppress("UNCHECKED_CAST")
        val favIds = params[3] as List<String>

        menuRepo.getFoodItemsForHall(hallId).map { list ->
            list.filter { item ->
                (!favOnly || favIds.contains(item.id)) &&
                (cat == null || item.category == cat) &&
                (query.isBlank() || item.name.contains(query, ignoreCase = true) || item.description.contains(query, ignoreCase = true))
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchChanged(q: String) { searchQuery.value = q }
    fun onCategorySelect(cat: FoodCategory?) {
        showFavoritesOnly.value = false
        selectedCategory.value = if (selectedCategory.value == cat) null else cat
    }

    fun toggleFavoritesFilter() {
        selectedCategory.value = null
        showFavoritesOnly.update { !it }
    }

    fun addToCart(foodItem: FoodItem) {
        cartRepo.addToCart(foodItem)
    }
}

class FoodDetailViewModel(
    private val foodId: String,
    private val menuRepo: IMenuRepository = MessQApplication.instance.container.menuRepository,
    private val cartRepo: ICartRepository = MessQApplication.instance.container.cartRepository,
    private val userRepo: IUserRepository = MessQApplication.instance.container.userRepository
) : ViewModel() {
    val foodItem = MutableStateFlow<FoodItem?>(null)
    val quantity = MutableStateFlow(1)
    val selectedAddOns = MutableStateFlow<Set<String>>(emptySet())
    val isFavorite: StateFlow<Boolean> = userRepo.userProfileFlow
        .map { profile -> profile.favoriteFoodIds.contains(foodId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        viewModelScope.launch {
            foodItem.value = menuRepo.getFoodItemById(foodId)
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            userRepo.toggleFavoriteFood(foodId)
        }
    }

    fun incrementQty() { quantity.value += 1 }
    fun decrementQty() { if (quantity.value > 1) quantity.value -= 1 }

    fun toggleAddOn(addOnId: String) {
        selectedAddOns.update { current ->
            if (current.contains(addOnId)) current - addOnId else current + addOnId
        }
    }

    fun addToCart(onAdded: () -> Unit) {
        val item = foodItem.value ?: return
        val chosenAddOns = item.addOns.filter { selectedAddOns.value.contains(it.id) }
        repeat(quantity.value) {
            cartRepo.addToCart(item, chosenAddOns)
        }
        onAdded()
    }
}

fun generateDynamicPickupSlots(): List<String> {
    val cal = Calendar.getInstance()
    val minute = cal.get(Calendar.MINUTE)
    val remainder = minute % 5
    if (remainder != 0) {
        cal.add(Calendar.MINUTE, 5 - remainder)
    }

    val format = SimpleDateFormat("h:mm a", Locale.getDefault())
    val slots = mutableListOf<String>()
    val offsets = listOf(10, 20, 30, 45, 60)

    for (offset in offsets) {
        val slotCal = cal.clone() as Calendar
        slotCal.add(Calendar.MINUTE, offset)
        slots.add(format.format(slotCal.time))
    }
    return slots
}

class CartViewModel(
    private val cartRepo: ICartRepository = MessQApplication.instance.container.cartRepository,
    private val orderRepo: IOrderRepository = MessQApplication.instance.container.orderRepository,
    private val userRepo: IUserRepository = MessQApplication.instance.container.userRepository
) : ViewModel() {
    val cart: StateFlow<Cart> = cartRepo.cartFlow
    val pickupSlots: List<String> = generateDynamicPickupSlots()
    val selectedSlot = MutableStateFlow(pickupSlots.firstOrNull() ?: "15 mins")
    val cookingNote = MutableStateFlow("")

    init {
        val initialSlot = pickupSlots.firstOrNull() ?: "15 mins"
        cartRepo.setPickupTime(initialSlot)
    }

    fun updateQuantity(itemId: String, delta: Int) {
        cartRepo.updateQuantity(itemId, delta)
    }

    fun setSlot(slot: String) {
        selectedSlot.value = slot
        cartRepo.setPickupTime(slot)
    }

    fun setNote(note: String) {
        cookingNote.value = note
        cartRepo.setCookingNote(note)
    }

    fun confirmOrder(onSuccess: (Order) -> Unit) {
        viewModelScope.launch {
            val user = userRepo.userProfileFlow.value
            val currentCart = cart.value
            if (currentCart.items.isNotEmpty()) {
                val order = orderRepo.placeOrder(currentCart, user)
                cartRepo.clearCart()
                onSuccess(order)
            }
        }
    }
}

class OrderViewModel(
    private val orderRepo: IOrderRepository = MessQApplication.instance.container.orderRepository
) : ViewModel() {
    val orders: StateFlow<List<Order>> = orderRepo.ordersFlow
    val selectedTab = MutableStateFlow(0) // 0: Active, 1: History

    val activeOrders: StateFlow<List<Order>> = orders.map { list ->
        list.filter { it.status != OrderStatus.COMPLETED && it.status != OrderStatus.CANCELLED }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pastOrders: StateFlow<List<Order>> = orders.map { list ->
        list.filter { it.status == OrderStatus.COMPLETED || it.status == OrderStatus.CANCELLED }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun reorder(order: Order, onSuccess: () -> Unit) {
        viewModelScope.launch {
            orderRepo.reorder(order)
            onSuccess()
        }
    }

    fun rateOrder(orderId: String, stars: Int) {
        viewModelScope.launch {
            orderRepo.rateOrder(orderId, stars)
        }
    }
}

class MapViewModel(
    private val diningRepo: IDiningRepository = MessQApplication.instance.container.diningRepository
) : ViewModel() {
    val diningHalls: StateFlow<List<DiningHall>> = diningRepo.getDiningHalls()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedHall = MutableStateFlow<DiningHall?>(null)
    val isNavigating = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            selectedHall.value = diningHalls.firstOrNull()?.firstOrNull()
        }
    }

    fun selectHall(hall: DiningHall) {
        selectedHall.value = hall
    }

    fun toggleNavigation() {
        isNavigating.update { !it }
    }
}

enum class ChatSender { USER, AI }

data class AiChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: ChatSender,
    val text: String,
    val timestamp: String = "",
    val actionHallId: String? = null,
    val actionHallName: String? = null
)

class AiAssistantViewModel(
    private val aiRepo: IAiAssistantRepository = MessQApplication.instance.container.aiAssistantRepository,
    private val diningRepo: IDiningRepository = MessQApplication.instance.container.diningRepository,
    private val menuRepo: IMenuRepository = MessQApplication.instance.container.menuRepository
) : ViewModel() {
    val promptChips = listOf(
        "📊 What hours have lowest crowd?",
        "⚡ Shortest Queue Right Now",
        "🎯 Best Veg Thali under ₹100",
        "☕ Best Coffee & Snacks",
        "🌿 Quiet Places near BH-4"
    )

    val inputQuery = MutableStateFlow("")
    val isThinking = MutableStateFlow(false)

    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    private fun currentTime(): String = timeFormat.format(Calendar.getInstance().time)

    private val systemInstructions = """
        You are MessQ AI, an intelligent, friendly, 1-on-1 dining assistant for Lovely Professional University (LPU) campus students.
        Answer conversationally like ChatGPT/Gemini, with clear time predictions, prices, and queue advice.
        
        Campus Dining Halls & Hourly Crowd Schedules:
        1. Main Dining Hall (#148) [ID: dh_1]:
           - Peak Crowd Hours: 12:30 PM - 1:45 PM (20-25 min wait) and 8:00 PM - 9:15 PM (18 min wait).
           - Lowest Crowd Hours: 11:00 AM - 12:00 PM (3 min wait) and 2:15 PM - 4:00 PM (4 min wait).
           - Menu Highlights: Rajma Rice (₹70), Paneer Butter Masala (₹80), Paneer Combo (₹89).
        2. Food Court A [ID: dh_2]:
           - Peak Crowd Hours: 1:00 PM - 2:15 PM (15-20 min wait).
           - Lowest Crowd Hours: 11:30 AM - 12:30 PM (5 min wait) and 3:00 PM - 5:00 PM (4 min wait).
           - Menu Highlights: Veg Hakka Noodles (₹90), Paneer Tikka Pizza (₹140).
        3. Food Street [ID: dh_3]:
           - Peak Crowd Hours: 5:00 PM - 7:00 PM (Evening snacks - 15 min wait).
           - Lowest Crowd Hours: 2:00 PM - 4:30 PM (3 min wait).
           - Menu Highlights: Steam Veg Momos (₹60), Aloo Paratha (₹65). Located 2 min walk from BH-4 hostel.
        4. Café Express [ID: dh_4]:
           - Peak Crowd Hours: 4:30 PM - 6:00 PM (8 min wait).
           - Lowest Crowd Hours: 11:00 AM - 3:30 PM (3-4 min wait - Shortest queue on campus).
           - Menu Highlights: Cold Coffee with Ice Cream (₹70), Grilled Cheese Sandwich (₹85).
    """.trimIndent()

    val chatMessages = MutableStateFlow<List<AiChatMessage>>(
        listOf(
            AiChatMessage(
                sender = ChatSender.AI,
                text = "Hey there! 👋 I'm MessQ AI, your 1-on-1 personal LPU dining assistant. Ask me anything about live wait times, hourly crowd forecasts, budget thalis, or coffee spots!",
                timestamp = currentTime()
            ),
            AiChatMessage(
                sender = ChatSender.AI,
                text = "📊 Tip: Main Mess (#148) peaks at 12:30 PM–1:45 PM (~25m wait), but has lowest crowd between 11:15 AM–12:00 PM and 2:15 PM–4:00 PM (~3m wait)!",
                timestamp = currentTime(),
                actionHallId = "dh_1",
                actionHallName = "Main Dining Hall"
            )
        )
    )

    fun sendMessage(userText: String) {
        val trimmed = userText.trim()
        if (trimmed.isBlank()) return

        val userMsg = AiChatMessage(
            sender = ChatSender.USER,
            text = trimmed,
            timestamp = currentTime()
        )
        chatMessages.update { it + userMsg }
        inputQuery.value = ""
        isThinking.value = true

        viewModelScope.launch {
            val reply = generateAiReply(trimmed)
            isThinking.value = false
            chatMessages.update { it + reply }
        }
    }

    fun sendPromptChip(chip: String) {
        sendMessage(chip)
    }

    private suspend fun generateAiReply(query: String): AiChatMessage = withContext(Dispatchers.IO) {
        val q = query.lowercase()
        val now = currentTime()

        try {
            val apiKey = "GEMINI_API_KEY_MESSQ_LPU"
            val generativeModel = GenerativeModel(
                modelName = "gemini-1.5-flash",
                apiKey = apiKey
            )
            val fullPrompt = "$systemInstructions\n\nStudent Query: $query\nProvide a friendly, helpful 2-4 sentence answer."
            val response = generativeModel.generateContent(fullPrompt)
            val responseText = response.text?.trim()
            if (!responseText.isNullOrBlank()) {
                val actionId = when {
                    q.contains("coffee") || q.contains("cafe") || q.contains("dh_4") -> "dh_4"
                    q.contains("court") || q.contains("dh_2") -> "dh_2"
                    q.contains("street") || q.contains("momo") || q.contains("bh-4") || q.contains("dh_3") -> "dh_3"
                    else -> "dh_1"
                }
                return@withContext AiChatMessage(
                    sender = ChatSender.AI,
                    text = responseText,
                    timestamp = now,
                    actionHallId = actionId,
                    actionHallName = if (actionId == "dh_4") "Café Express" else "Main Dining Hall"
                )
            }
        } catch (_: Throwable) {
            // Fallback to local intelligent search & recommendation engine
        }

        // Dynamic search across all menu food items & categories
        val allItems = menuRepo.getAllFoodItems().firstOrNull() ?: emptyList()

        // 1. Extract numeric budget limit (e.g. "under 50", "anything under 50 ruppee", "below 80 rs")
        val extractedNumber = Regex("""\b(\d+)\b""").find(q)?.groupValues?.get(1)?.toDoubleOrNull()
        val isPriceIntent = extractedNumber != null && (q.contains("budget") || q.contains("cheap") || q.contains("rupee") || q.contains("ruppee") || q.contains("rs") || q.contains("₹") || q.contains("under") || q.contains("below") || q.contains("less") || q.contains("within") || q.contains("anything"))

        if (extractedNumber != null && isPriceIntent) {
            val maxPrice = extractedNumber
            val budgetMatches = allItems.filter { it.price <= maxPrice }
            if (budgetMatches.isNotEmpty()) {
                val formattedList = budgetMatches.sortedBy { it.price }.take(4).joinToString("\n") { "• ${it.name} (₹${it.price.toInt()}) at ${it.diningHallName}" }
                val bestItem = budgetMatches.minByOrNull { it.price } ?: budgetMatches.first()
                return@withContext AiChatMessage(
                    sender = ChatSender.AI,
                    text = "🎯 Delicious options under ₹${maxPrice.toInt()} at MessQ LPU:\n$formattedList",
                    timestamp = now,
                    actionHallId = bestItem.diningHallId,
                    actionHallName = bestItem.diningHallName
                )
            } else {
                val cheapest = allItems.minByOrNull { it.price }
                val cheapestText = if (cheapest != null) " Our lowest price item is '${cheapest.name}' at ₹${cheapest.price.toInt()}." else ""
                return@withContext AiChatMessage(
                    sender = ChatSender.AI,
                    text = "🎯 No items found under ₹${maxPrice.toInt()}.$cheapestText Would you like to check items under ₹70 like Rajma Rice or Chole Bhature?",
                    timestamp = now,
                    actionHallId = cheapest?.diningHallId ?: "dh_1",
                    actionHallName = cheapest?.diningHallName ?: "Main Dining Hall"
                )
            }
        }

        val matchedItems = allItems.mapNotNull { item ->
            val itemNameLower = item.name.lowercase()
            val score = when {
                q.trim() == itemNameLower -> 100
                q.contains(itemNameLower) -> 90
                itemNameLower.contains(q) -> 80
                q.split(" ").any { word -> word.length > 2 && itemNameLower.contains(word) } -> 60
                q.contains(item.category.name.lowercase()) -> 30
                q.contains(item.category.displayName.lowercase()) -> 30
                else -> 0
            }
            if (score > 0) Pair(item, score) else null
        }

        if (matchedItems.isNotEmpty()) {
            val topMatch = matchedItems.maxByOrNull { it.second }!!.first
            val isNonVegSearch = q.contains("chicken") || q.contains("mutton") || q.contains("egg") || q.contains("non-veg") || q.contains("non veg")
            val vegNote = if (topMatch.isVeg && isNonVegSearch) " (Note: Campus dining halls serve 100% pure Veg options)" else ""

            return@withContext AiChatMessage(
                sender = ChatSender.AI,
                text = "🍲 Found '${topMatch.name}' at ${topMatch.diningHallName} for ₹${topMatch.price.toInt()} (⭐ ${topMatch.rating} rating)!$vegNote\n• ${topMatch.description}",
                timestamp = now,
                actionHallId = topMatch.diningHallId,
                actionHallName = topMatch.diningHallName
            )
        }

        val fallbackMsg = when {
            q.contains("hour") || q.contains("crowd") || q.contains("time") || q.contains("when") || q.contains("less") || q.contains("more") -> AiChatMessage(
                sender = ChatSender.AI,
                text = "📊 Hourly Crowd Forecast:\n• Main Mess (#148): PEAK crowd 12:30 PM–1:45 PM (20–25 min wait). LOWEST crowd 11:00 AM–12:00 PM and 2:15 PM–4:00 PM (3–4 min wait).\n• Food Court A: PEAK crowd 1:00 PM–2:15 PM. LOWEST crowd 3:00 PM–5:00 PM.\n• Food Street: PEAK crowd 5:00 PM–7:00 PM (Evening snacks). LOWEST crowd 2:00 PM–4:30 PM.\n• Café Express: Shortest wait time on campus overall (~4 mins)!",
                timestamp = now,
                actionHallId = "dh_1",
                actionHallName = "Main Dining Hall"
            )
            q.contains("shortest") || q.contains("quick") || q.contains("fast") -> AiChatMessage(
                sender = ChatSender.AI,
                text = "⚡ Café Express currently has the shortest wait time on campus (~4 mins). Main Dining Hall #148 is experiencing peak lunch hours (~15 mins wait).",
                timestamp = now,
                actionHallId = "dh_4",
                actionHallName = "Café Express"
            )
            q.contains("thali") || q.contains("veg") || q.contains("100") || q.contains("budget") || q.contains("cheap") -> AiChatMessage(
                sender = ChatSender.AI,
                text = "🎯 Main Dining Hall has the Paneer Combo at ₹89 and Rajma Rice at ₹70! 100% pure veg, delicious, and fits well under your ₹100 budget.",
                timestamp = now,
                actionHallId = "dh_1",
                actionHallName = "Main Dining Hall"
            )
            q.contains("coffee") || q.contains("snack") || q.contains("tea") || q.contains("sandwich") -> AiChatMessage(
                sender = ChatSender.AI,
                text = "☕ I recommend Cold Coffee with Ice Cream (₹70) and Grilled Cheese Sandwich (₹85) at Café Express! Great vibe and fast prep time.",
                timestamp = now,
                actionHallId = "dh_4",
                actionHallName = "Café Express"
            )
            q.contains("bh-4") || q.contains("quiet") || q.contains("hostel") || q.contains("near") -> AiChatMessage(
                sender = ChatSender.AI,
                text = "🌿 Food Street is located just a 2-minute walk from BH-4 hostel! Grab fresh Steam Veg Momos (₹60) with shaded outdoor seating.",
                timestamp = now,
                actionHallId = "dh_3",
                actionHallName = "Food Street"
            )
            else -> AiChatMessage(
                sender = ChatSender.AI,
                text = "I'm on it! Based on real-time campus data, Main Dining Hall (#148) is currently serving token #124. For a faster meal, check out Café Express or Food Court A!",
                timestamp = now,
                actionHallId = "dh_1",
                actionHallName = "Main Dining Hall"
            )
        }
        return@withContext fallbackMsg
    }
}

class ProfileViewModel(
    private val userRepo: IUserRepository = MessQApplication.instance.container.userRepository,
    private val menuRepo: IMenuRepository = MessQApplication.instance.container.menuRepository,
    private val cartRepo: ICartRepository = MessQApplication.instance.container.cartRepository,
    private val diningRepo: IDiningRepository = MessQApplication.instance.container.diningRepository
) : ViewModel() {
    val userProfile: StateFlow<UserProfile> = userRepo.userProfileFlow
    val isDarkMode: StateFlow<Boolean> = userRepo.isDarkModeFlow
    val myReservations: StateFlow<List<TableReservation>> = diningRepo.reservationsFlow

    val favoriteFoods: StateFlow<List<FoodItem>> = combine(
        userRepo.userProfileFlow,
        menuRepo.getAllFoodItems()
    ) { profile, allFoods ->
        allFoods.filter { profile.favoriteFoodIds.contains(it.id) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun cancelReservation(id: String) {
        viewModelScope.launch {
            diningRepo.cancelReservation(id)
        }
    }

    fun removeFavorite(foodId: String) {
        viewModelScope.launch {
            userRepo.toggleFavoriteFood(foodId)
        }
    }

    fun quickAddToCart(food: FoodItem) {
        cartRepo.addToCart(food)
    }

    fun setDietaryPreference(pref: DietaryPreference) {
        viewModelScope.launch {
            userRepo.updateDietaryPreference(pref)
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            userRepo.toggleDarkMode(enabled)
        }
    }

    fun updateProfile(
        name: String,
        email: String,
        studentId: String,
        department: String,
        hostel: String
    ) {
        viewModelScope.launch {
            val current = userProfile.value
            userRepo.updateProfile(
                current.copy(
                    name = name.trim().ifEmpty { current.name },
                    email = email.trim().ifEmpty { current.email },
                    studentId = studentId.trim().ifEmpty { current.studentId },
                    department = department.trim().ifEmpty { current.department },
                    hostel = hostel.trim().ifEmpty { current.hostel }
                )
            )
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            val current = userProfile.value
            userRepo.updateProfile(current.copy(notificationsEnabled = enabled))
        }
    }

    fun setAvatar(avatarId: String) {
        viewModelScope.launch {
            userRepo.updateAvatar(avatarId)
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            userRepo.logout()
            onLoggedOut()
        }
    }
}

class AdminViewModel(
    private val adminRepo: IAdminRepository = MessQApplication.instance.container.adminRepository,
    private val diningRepo: IDiningRepository = MessQApplication.instance.container.diningRepository
) : ViewModel() {
    val metrics: StateFlow<AdminDashboardMetrics> = adminRepo.metricsFlow
    val diningHalls: StateFlow<List<DiningHall>> = diningRepo.getDiningHalls()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun advanceToken(hallId: String) {
        viewModelScope.launch {
            adminRepo.advanceToken(hallId)
        }
    }

    fun updateCrowd(hallId: String, crowd: CrowdLevel, waitMin: Int) {
        viewModelScope.launch {
            adminRepo.updateHallStatus(hallId, crowd, waitMin)
        }
    }
}
