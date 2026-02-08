package com.example.zyberauto.data.repository

import com.example.zyberauto.data.local.LocalDataHelper
import com.example.zyberauto.domain.model.Appointment
import com.example.zyberauto.domain.repository.AppointmentsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppointmentsRepositoryImpl @Inject constructor(
    private val localDataHelper: LocalDataHelper
) : AppointmentsRepository {

    private val appointmentsFile = "appointments.json"

    override fun getAppointments(): Flow<List<Appointment>> = flow {
        val appointments = localDataHelper.readList(appointmentsFile, Appointment::class.java)
        emit(appointments.sortedBy { it.dateScheduled })
    }

    override fun getBookingsForUser(userId: String): Flow<List<Appointment>> = flow {
        val appointments = localDataHelper.readList(appointmentsFile, Appointment::class.java)
        emit(appointments.filter { it.userId == userId }.sortedByDescending { it.dateScheduled })
    }

    override suspend fun createBooking(appointment: Appointment) {
        val finalAppointment = if (appointment.id.isBlank()) {
            appointment.copy(id = System.currentTimeMillis().toString())
        } else {
            appointment
        }
        localDataHelper.addItem(appointmentsFile, finalAppointment, Appointment::class.java)
    }

    override suspend fun updateAppointmentStatus(appointmentId: String, status: String) {
        localDataHelper.updateItem(
            appointmentsFile,
            Appointment::class.java,
            predicate = { it.id == appointmentId },
            update = { it.copy(status = status) }
        )
    }

    override suspend fun rejectBooking(appointmentId: String, reason: String) {
        localDataHelper.updateItem(
            appointmentsFile,
            Appointment::class.java,
            predicate = { it.id == appointmentId },
            update = { it.copy(status = "DECLINED", rejectionReason = reason) }
        )
    }

    override suspend fun acceptAppointment(appointmentId: String, mechanicNumber: Int) {
        localDataHelper.updateItem(
            appointmentsFile,
            Appointment::class.java,
            predicate = { it.id == appointmentId },
            update = { it.copy(status = "ACCEPTED", assignedMechanic = mechanicNumber) }
        )
    }

    override suspend fun hasActiveBookingForVehicle(plateNumber: String): Boolean {
        val appointments = localDataHelper.readList(appointmentsFile, Appointment::class.java)
        return appointments.any { 
            it.plateNumber.equals(plateNumber, ignoreCase = true) &&
            (it.status == "PENDING" || it.status == "ACCEPTED")
        }
    }

    override suspend fun getAssignedMechanicsForSlot(date: Date, timeSlot: String): List<Int> {
        val appointments = localDataHelper.readList(appointmentsFile, Appointment::class.java)
        
        val targetCal = Calendar.getInstance().apply { time = date }
        val targetDay = targetCal.get(Calendar.DAY_OF_YEAR)
        val targetYear = targetCal.get(Calendar.YEAR)
        
        return appointments.filter { appointment ->
            if (appointment.status != "ACCEPTED" || appointment.assignedMechanic <= 0) return@filter false
            
            val appCal = Calendar.getInstance().apply { time = appointment.dateScheduled }
            val sameDay = appCal.get(Calendar.DAY_OF_YEAR) == targetDay && 
                          appCal.get(Calendar.YEAR) == targetYear
            val sameSlot = appointment.timeSlot.equals(timeSlot, ignoreCase = true)
            
            sameDay && sameSlot
        }.map { it.assignedMechanic }.distinct()
    }

    private suspend fun countAppointmentsForSlot(date: Date, timeSlot: String): Int {
        val appointments = localDataHelper.readList(appointmentsFile, Appointment::class.java)
        
        val targetCal = Calendar.getInstance().apply { time = date }
        val targetDay = targetCal.get(Calendar.DAY_OF_YEAR)
        val targetYear = targetCal.get(Calendar.YEAR)
        
        return appointments.count { appointment ->
            if (appointment.status != "PENDING" && appointment.status != "ACCEPTED") return@count false
            
            val appCal = Calendar.getInstance().apply { time = appointment.dateScheduled }
            val sameDay = appCal.get(Calendar.DAY_OF_YEAR) == targetDay && 
                          appCal.get(Calendar.YEAR) == targetYear
            val sameSlot = appointment.timeSlot.equals(timeSlot, ignoreCase = true)
            
            sameDay && sameSlot
        }
    }

    override suspend fun isTimeSlotFullyBooked(date: Date, timeSlot: String): Boolean {
        return countAppointmentsForSlot(date, timeSlot) >= 3
    }

    override suspend fun getAvailableMechanicsForSlot(date: Date, timeSlot: String): List<Int> {
        val assignedMechanics = getAssignedMechanicsForSlot(date, timeSlot)
        return listOf(1, 2, 3).filter { it !in assignedMechanics }
    }

    override suspend fun rescheduleAppointment(
        appointmentId: String,
        newDate: Date,
        newTimeSlot: String,
        newTimeSlotEnd: String,
        estimatedDurationMinutes: Int
    ) {
        localDataHelper.updateItem(
            appointmentsFile,
            Appointment::class.java,
            predicate = { it.id == appointmentId },
            update = { 
                it.copy(
                    dateScheduled = newDate,
                    timeSlot = newTimeSlot,
                    timeSlotEnd = newTimeSlotEnd,
                    estimatedDurationMinutes = estimatedDurationMinutes,
                    status = "PENDING",
                    assignedMechanic = 0
                )
            }
        )
    }
}
