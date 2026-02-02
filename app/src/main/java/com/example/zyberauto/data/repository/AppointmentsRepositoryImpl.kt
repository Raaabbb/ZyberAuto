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
    private val firestore: FirebaseFirestore
) : AppointmentsRepository {

    override fun getAppointments(): Flow<List<Appointment>> = callbackFlow {
        val listener = firestore.collection("appointments")
            .orderBy("dateScheduled", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val appointments = snapshot?.toObjects(Appointment::class.java) ?: emptyList()
                trySend(appointments)
            }
        
        awaitClose { listener.remove() }
    }

    override fun getBookingsForUser(userId: String): Flow<List<Appointment>> = callbackFlow {
        var fallbackListener: com.google.firebase.firestore.ListenerRegistration? = null
        
        val indexedListener = firestore.collection("appointments")
            .whereEqualTo("userId", userId)
            .orderBy("dateScheduled", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Check for missing index error
                    if (error.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.FAILED_PRECONDITION) {
                        android.util.Log.w("ApptRepo", "Index missing, falling back to client-side sorting")
                        
                        fallbackListener = firestore.collection("appointments")
                            .whereEqualTo("userId", userId)
                            .addSnapshotListener { fbSnap, fbErr ->
                                if (fbErr != null) {
                                    close(fbErr)
                                    return@addSnapshotListener
                                }
                                val rawList = fbSnap?.toObjects(Appointment::class.java) ?: emptyList()
                                trySend(rawList.sortedByDescending { it.dateScheduled })
                            }
                    } else {
                        close(error)
                    }
                    return@addSnapshotListener
                }
                
                val appointments = snapshot?.toObjects(Appointment::class.java) ?: emptyList()
                trySend(appointments)
            }
        
        awaitClose { 
            indexedListener.remove()
            fallbackListener?.remove()
        }
    }

    override suspend fun createBooking(appointment: Appointment) {
        firestore.collection("appointments").add(appointment).await()
    }

    override suspend fun updateAppointmentStatus(appointmentId: String, status: String) {
        firestore.collection("appointments").document(appointmentId)
            .update("status", status)
            .await()
    }

    override suspend fun rejectBooking(appointmentId: String, reason: String) {
        firestore.collection("appointments").document(appointmentId)
            .update(
                mapOf(
                    "status" to "DECLINED",
                    "rejectionReason" to reason
                )
            )
            .await()
    }
}
