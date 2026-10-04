package com.example.messqlpu

import com.example.messqlpu.data.local.MessQDatabase
import com.example.messqlpu.data.repository.UserRepositoryImpl
import com.example.messqlpu.domain.model.DietaryPreference
import com.example.messqlpu.domain.model.UserProfile
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class ProfileUpdateTest {

    @Test
    fun testUpdateProfileFields() = runBlocking {
        val userRepo = UserRepositoryImpl()
        val initialProfile = userRepo.userProfileFlow.value

        assertEquals("12108934", initialProfile.studentId)

        val updatedProfile = UserProfile(
            name = "Rohan Verma",
            email = "rohan.v@lpu.in",
            studentId = "12009876",
            department = "School of Electronics",
            hostel = "BH-2, Room 410",
            dietaryPreference = DietaryPreference.NON_VEG
        )

        userRepo.updateProfile(updatedProfile)

        val current = userRepo.userProfileFlow.value
        assertEquals("Rohan Verma", current.name)
        assertEquals("rohan.v@lpu.in", current.email)
        assertEquals("12009876", current.studentId)
        assertEquals("School of Electronics", current.department)
        assertEquals("BH-2, Room 410", current.hostel)
        assertEquals(DietaryPreference.NON_VEG, current.dietaryPreference)
    }

    @Test
    fun testDarkModeToggle() = runBlocking {
        val userRepo = UserRepositoryImpl()
        assertFalse(userRepo.isDarkModeFlow.value)

        userRepo.toggleDarkMode(true)
        assertTrue(userRepo.isDarkModeFlow.value)
        assertTrue(userRepo.userProfileFlow.value.darkModeEnabled)

        userRepo.toggleDarkMode(false)
        assertFalse(userRepo.isDarkModeFlow.value)
        assertFalse(userRepo.userProfileFlow.value.darkModeEnabled)
    }

    @Test
    fun testAvatarSelection() = runBlocking {
        val userRepo = UserRepositoryImpl()
        assertEquals("avatar_scholar", userRepo.userProfileFlow.value.avatarId)

        userRepo.updateAvatar("avatar_robo")
        assertEquals("avatar_robo", userRepo.userProfileFlow.value.avatarId)

        userRepo.updateAvatar("avatar_foodie")
        assertEquals("avatar_foodie", userRepo.userProfileFlow.value.avatarId)
    }
}
