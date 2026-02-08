package com.example.zyberauto.di

import com.example.zyberauto.data.repository.UserRepositoryImpl
import com.example.zyberauto.domain.repository.UserRepository
import com.example.zyberauto.data.repository.AppointmentsRepositoryImpl
import com.example.zyberauto.domain.repository.AppointmentsRepository
import com.example.zyberauto.domain.repository.InventoryRepository
import com.example.zyberauto.domain.service.CostEstimationService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseDatabase(): com.google.firebase.database.FirebaseDatabase {
        val database = com.google.firebase.database.FirebaseDatabase.getInstance("https://wowo-4737e9d8-default-rtdb.asia-southeast1.firebasedatabase.app/")
        database.setPersistenceEnabled(true)
        return database
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): UserRepository {
        return UserRepositoryImpl(auth, firestore)
    }

    @Provides
    @Singleton
    fun provideAppointmentsRepository(
        database: com.google.firebase.database.FirebaseDatabase
    ): AppointmentsRepository {
        return AppointmentsRepositoryImpl(database)
    }



    @Provides
    @Singleton
    fun provideComplaintsRepository(
        firestore: FirebaseFirestore
    ): com.example.zyberauto.domain.repository.ComplaintsRepository {
        return com.example.zyberauto.data.repository.ComplaintsRepositoryImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideInventoryRepository(
        firestore: FirebaseFirestore
    ): com.example.zyberauto.domain.repository.InventoryRepository {
        return com.example.zyberauto.data.repository.InventoryRepositoryImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideVehiclesRepository(
        firestore: com.google.firebase.firestore.FirebaseFirestore
    ): com.example.zyberauto.domain.repository.VehiclesRepository {
        return com.example.zyberauto.data.repository.VehiclesRepositoryImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideChatRepository(
        database: com.google.firebase.database.FirebaseDatabase
    ): com.example.zyberauto.domain.repository.ChatRepository {
        return com.example.zyberauto.data.repository.ChatRepositoryImpl(database)
    }

    @Provides
    @Singleton
    fun provideCostEstimationService(
        inventoryRepository: InventoryRepository
    ): CostEstimationService {
        return CostEstimationService(inventoryRepository)
    }
}
