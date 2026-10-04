package com.example.messqlpu.domain.model

enum class FoodCategory(val displayName: String) {
    NORTH_INDIAN("North Indian"),
    SOUTH_INDIAN("South Indian"),
    CHINESE("Chinese"),
    SNACKS("Snacks"),
    BEVERAGES("Beverages"),
    DESSERT("Desserts")
}

data class FoodAddOn(
    val id: String,
    val name: String,
    val price: Double,
    val isSelected: Boolean = false
)

data class FoodItem(
    val id: String,
    val diningHallId: String,
    val diningHallName: String,
    val name: String,
    val category: FoodCategory,
    val price: Double,
    val rating: Float = 4.5f,
    val reviewCount: Int = 120,
    val description: String,
    val imageUrl: String,
    val isVeg: Boolean = true,
    val calories: Int = 380,
    val proteinG: Int = 14,
    val fatG: Int = 10,
    val carbsG: Int = 54,
    val isAvailable: Boolean = true,
    val preparationTimeMin: Int = 8,
    val addOns: List<FoodAddOn> = emptyList()
)

data class CartItem(
    val foodItem: FoodItem,
    val quantity: Int = 1,
    val selectedAddOns: List<FoodAddOn> = emptyList(),
    val specialInstructions: String = ""
) {
    val itemTotalPrice: Double
        get() {
            val addOnTotal = selectedAddOns.sumOf { it.price }
            return (foodItem.price + addOnTotal) * quantity
        }
}

data class Cart(
    val items: List<CartItem> = emptyList(),
    val diningHallId: String = "",
    val diningHallName: String = "",
    val pickupTimeSlot: String = "12:45 PM",
    val cookingNote: String = ""
) {
    val subtotal: Double
        get() = items.sumOf { it.itemTotalPrice }

    val gstTax: Double
        get() = subtotal * 0.05 // 5% GST

    val platformFee: Double
        get() = if (items.isNotEmpty()) 2.0 else 0.0

    val total: Double
        get() = if (items.isNotEmpty()) subtotal + gstTax + platformFee else 0.0

    val totalItemCount: Int
        get() = items.sumOf { it.quantity }
}
