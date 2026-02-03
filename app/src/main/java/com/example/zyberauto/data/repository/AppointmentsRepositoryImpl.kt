package com.example.zyberauto.data.repository

import com.example.zyberauto.domain.model.Appointment
import com.example.zyberauto.domain.repository.AppointmentsRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject

class AppointmentsRepositoryImpl @Inject constructor(
    private val database: com.google.firebase.database.FirebaseDatabase
) : AppointmentsRepository {

    override fun getAppointments(): Flow<List<Appointment>> = callbackFlow {
        val ref = database.getReference("appointments")
        val listener = object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val appointments = snapshot.children.mapNotNull { 
                    it.getValue(Appointment::class.java)?.copy(id = it.key ?: "") 
                }.sortedBy { it.dateScheduled }
                trySend(appointments)
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    override fun getBookingsForUser(userId: String): Flow<List<Appointment>> = callbackFlow {
        val ref = database.getReference("appointments")
        // Query by userId
        val query = ref.orderByChild("userId").equalTo(userId)
        
        val listener = object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val appointments = snapshot.children.mapNotNull { 
                    it.getValue(Appointment::class.java)?.copy(id = it.key ?: "") 
                }.sortedByDescending { it.dateScheduled } // Client-side sort
                trySend(appointments)
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                close(error.toException())
            }
        }
        query.addValueEventListener(listener)
        awaitClose { query.removeEventListener(listener) }
    }

    override suspend fun createBooking(appointment: Appointment) {
        val ref = database.getReference("appointments").push()
        // Save with ID if needed, or let push generate key and update it
        val appointmentWithId = appointment.copy(id = ref.key ?: "")
        ref.setValue(appointmentWithId).await()
    }

    override suspend fun updateAppointmentStatus(appointmentId: String, status: String) {
        database.getReference("appointments").child(appointmentId)
            .updateChildren(mapOf("status" to status))
            .await()
    }

    override suspend fun rejectBooking(appointmentId: String, reason: String) {
        database.getReference("appointments").child(appointmentId)
            .updateChildren(
                mapOf(
                    "status" to "DECLINED",
                    "rejectionReason" to reason
                )
            )
            .await()
    }
}
