package com.example.messqlpu

import com.example.messqlpu.data.MockDataProvider
import com.example.messqlpu.data.local.MessQDatabase
import com.example.messqlpu.data.repository.CartRepositoryImpl
import com.example.messqlpu.data.repository.QueueRepositoryImpl
import com.example.messqlpu.domain.model.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class MessQUnitTest {

    @Test
    fun testCartItemCalculationAndGst() {
        val cartRepo = CartRepositoryImpl()
        cartRepo.clearCart()
        val food = MockDataProvider.sampleFoodItems.first() // Rajma Rice ₹70

        cartRepo.addToCart(food)
        var cart = cartRepo.cartFlow.value
        assertEquals(1, cart.items.size)
        assertEquals(70.0, cart.subtotal, 0.01)
        assertEquals(3.5, cart.gstTax, 0.01) // 5% GST
        assertEquals(2.0, cart.platformFee, 0.01)
        assertEquals(75.5, cart.total, 0.01)

        // Increment quantity
        cartRepo.updateQuantity(food.id, 1)
        cart = cartRepo.cartFlow.value
        assertEquals(2, cart.items.first().quantity)
        assertEquals(140.0, cart.subtotal, 0.01)

        // Decrement quantity twice to remove
        cartRepo.updateQuantity(food.id, -1)
        cart = cartRepo.cartFlow.value
        assertEquals(1, cart.items.first().quantity)

        cartRepo.updateQuantity(food.id, -1)
        cart = cartRepo.cartFlow.value
        assertTrue(cart.items.isEmpty())
        assertEquals(0.0, cart.total, 0.01)
    }

    @Test
    fun testQueueJoinAndAdvance() = runBlocking {
        val db = MessQDatabase()
        val queueRepo = QueueRepositoryImpl(db)
        val mainHall = MockDataProvider.sampleDiningHalls.first()

        // Join queue
        val token = queueRepo.joinQueue(mainHall, "12108934", "Aarav Sharma")
        assertEquals(mainHall.estimatedToken, token.tokenNumber)
        assertEquals(QueueStatus.WAITING, token.status)
        assertEquals(mainHall.name, token.diningHallName)
        assertNotNull(queueRepo.activeTokenFlow.value)

        // Admin advances queue
        val initialServed = token.currentServingToken
        queueRepo.adminAdvanceQueue(mainHall.id)
        val updatedToken = queueRepo.activeTokenFlow.value
        assertNotNull(updatedToken)
        assertEquals(initialServed + 1, updatedToken!!.currentServingToken)

        // Leave queue
        queueRepo.leaveQueue()
        assertNull(queueRepo.activeTokenFlow.value)
    }

    @Test
    fun testDiningHallOccupancyAndCrowd() {
        val hall = MockDataProvider.sampleDiningHalls.first()
        assertTrue(hall.occupancyPercent in 0..100)
        assertEquals(CrowdLevel.LOW, hall.crowdLevel)
        assertTrue(hall.facilities.contains("AC Hall"))
    }

    @Test
    fun testAiRecommendationInsights() {
        val recommendations = MockDataProvider.sampleAiRecommendations
        assertTrue(recommendations.isNotEmpty())
        val cafeRecommendation = recommendations.find { it.diningHallId == "dh_4" }
        assertNotNull(cafeRecommendation)
        assertEquals(18, cafeRecommendation!!.savedMinutes)
        assertTrue(cafeRecommendation.reasons.isNotEmpty())
    }
}
