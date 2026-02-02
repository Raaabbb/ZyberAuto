package com.example.zyberauto.di

import com.example.zyberauto.data.repository.UserRepositoryImpl
import com.example.zyberauto.domain.repository.UserRepository
import com.example.zyberauto.data.repository.AppointmentsRepositoryImpl
import com.example.zyberauto.domain.repository.AppointmentsRepository
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
    fun provideUserRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): UserRepository {
        return UserRepositoryImpl(auth, firestore)
    }

    @Provides
    @Singleton
    fun provideAppointmentsRepository(
        firestore: FirebaseFirestore
    ): AppointmentsRepository {
        return AppointmentsRepositoryImpl(firestore)
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
}
