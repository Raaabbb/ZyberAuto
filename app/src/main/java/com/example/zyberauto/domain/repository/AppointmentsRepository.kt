package com.example.zyberauto.domain.repository

import com.example.zyberauto.domain.model.Appointment
import kotlinx.coroutines.flow.Flow

interface AppointmentsRepository {
    fun getAppointments(): Flow<List<Appointment>> // For Secretary (All)
    fun getBookingsForUser(userId: String): Flow<List<Appointment>> // For Customer (Filtered)
    suspend fun createBooking(appointment: Appointment)
    suspend fun updateAppointmentStatus(appointmentId: String, status: String)
    suspend fun rejectBooking(appointmentId: String, reason: String)
}
