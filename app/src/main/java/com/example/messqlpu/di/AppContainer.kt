package com.example.messqlpu.di

import com.example.messqlpu.data.local.MessQDatabase
import com.example.messqlpu.data.repository.*
import com.example.messqlpu.domain.repository.*

interface AppContainer {
    val diningRepository: IDiningRepository
    val queueRepository: IQueueRepository
    val menuRepository: IMenuRepository
    val cartRepository: ICartRepository
    val orderRepository: IOrderRepository
    val aiAssistantRepository: IAiAssistantRepository
    val userRepository: IUserRepository
    val adminRepository: IAdminRepository
}

class DefaultAppContainer : AppContainer {
    private val db: MessQDatabase by lazy {
        MessQDatabase.getInstance()
    }

    override val diningRepository: IDiningRepository by lazy {
        DiningRepositoryImpl(db)
    }

    override val queueRepository: IQueueRepository by lazy {
        QueueRepositoryImpl(db)
    }

    override val menuRepository: IMenuRepository by lazy {
        MenuRepositoryImpl(db)
    }

    override val cartRepository: ICartRepository by lazy {
        CartRepositoryImpl()
    }

    override val orderRepository: IOrderRepository by lazy {
        OrderRepositoryImpl(db)
    }

    override val aiAssistantRepository: IAiAssistantRepository by lazy {
        AiAssistantRepositoryImpl()
    }

    override val userRepository: IUserRepository by lazy {
        UserRepositoryImpl()
    }

    override val adminRepository: IAdminRepository by lazy {
        AdminRepositoryImpl(queueRepository, diningRepository)
    }
}
