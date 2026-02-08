package com.example.zyberauto.domain.repository

import com.example.zyberauto.domain.model.Appointment
import kotlinx.coroutines.flow.Flow
import java.util.Date

interface AppointmentsRepository {
    fun getAppointments(): Flow<List<Appointment>> // For Secretary (All)
    fun getBookingsForUser(userId: String): Flow<List<Appointment>> // For Customer (Filtered)
    suspend fun createBooking(appointment: Appointment)
    suspend fun updateAppointmentStatus(appointmentId: String, status: String)
    suspend fun acceptAppointment(appointmentId: String, mechanicNumber: Int) // Accept with mechanic assignment
    suspend fun rejectBooking(appointmentId: String, reason: String)
    
    // NEW: Booking validation functions
    /**
     * Check if a vehicle (by plate number) has any active booking (PENDING or ACCEPTED).
     * Returns true if there's an active booking, false if the vehicle can book again.
     */
    suspend fun hasActiveBookingForVehicle(plateNumber: String): Boolean
    
    /**
     * Get list of already assigned mechanics for a specific date and time slot.
     * Used to determine which mechanics are available for that slot.
     */
    suspend fun getAssignedMechanicsForSlot(date: Date, timeSlot: String): List<Int>
    
    /**
     * Check if a time slot is fully booked (all 3 mechanics assigned).
     */
    suspend fun isTimeSlotFullyBooked(date: Date, timeSlot: String): Boolean
    
    /**
     * Get available mechanics for a given date and time slot (mechanics not already assigned).
     */
    suspend fun getAvailableMechanicsForSlot(date: Date, timeSlot: String): List<Int>
    
    /**
     * Reschedule an appointment to a new date and time.
     * Updates dateScheduled, timeSlot, timeSlotEnd, and resets status to PENDING.
     */
    suspend fun rescheduleAppointment(
        appointmentId: String,
        newDate: Date,
        newTimeSlot: String,
        newTimeSlotEnd: String,
        estimatedDurationMinutes: Int
    )
}
