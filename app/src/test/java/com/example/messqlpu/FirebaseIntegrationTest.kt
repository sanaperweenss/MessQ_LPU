package com.example.messqlpu

import com.example.messqlpu.data.firebase.FirebaseManager
import com.example.messqlpu.domain.model.QueueStatus
import com.example.messqlpu.domain.model.QueueToken
import com.example.messqlpu.domain.model.UserProfile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class FirebaseIntegrationTest {

    @Test
    fun testNotificationEmissionAndFlow() = runBlocking {
        FirebaseManager.emitNotification(
            title = "Token #148 Ready",
            message = "Proceed to Counter 3 for pickup.",
            type = "TOKEN_CALLED"
        )

        val notification = FirebaseManager.fcmNotificationFlow.first()
        assertEquals("Token #148 Ready", notification.title)
        assertEquals("Proceed to Counter 3 for pickup.", notification.message)
        assertEquals("TOKEN_CALLED", notification.type)
    }

    @Test
    fun testUserProfileSyncFallback() = runBlocking {
        val testProfile = UserProfile(
            studentId = "12199999",
            name = "Priya Sharma",
            email = "priya.s@lpu.in",
            role = "Student",
            avatarId = "avatar_chef"
        )

        FirebaseManager.updateCurrentUser(testProfile)
        assertEquals("12199999", FirebaseManager.currentUser?.studentId)
        assertEquals("avatar_chef", FirebaseManager.currentUser?.avatarId)
        assertTrue(FirebaseManager.isUserLoggedIn())

        // Ensure sync does not crash in offline/fallback mode
        FirebaseManager.syncUserProfile(testProfile)
    }

    @Test
    fun testQueueTokenSyncFallback() = runBlocking {
        val token = QueueToken(
            tokenNumber = 149,
            diningHallId = "dh_1",
            diningHallName = "Main Dining Hall",
            studentId = "12199999",
            studentName = "Priya Sharma",
            status = QueueStatus.WAITING
        )

        // Ensure token sync does not throw
        FirebaseManager.syncQueueToken(token)
    }

    @Test
    fun testSignOut() {
        FirebaseManager.signOut()
        assertFalse(FirebaseManager.isUserLoggedIn())
    }
}
