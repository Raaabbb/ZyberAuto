package com.example.zyberauto.data.repository

import com.example.zyberauto.domain.model.Complaint
import com.example.zyberauto.domain.repository.ComplaintsRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ComplaintsRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ComplaintsRepository {

    override fun getComplaintsForUser(userId: String): Flow<List<Complaint>> = callbackFlow {
        var fallbackListener: com.google.firebase.firestore.ListenerRegistration? = null
        
        val indexedListener = firestore.collection("complaints")
            .whereEqualTo("userId", userId)
            .orderBy("dateSubmitted", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    if (e.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.FAILED_PRECONDITION) {
                        android.util.Log.w("ComplaintRepo", "Index missing, falling back to client-side sorting")
                        
                        fallbackListener = firestore.collection("complaints")
                            .whereEqualTo("userId", userId)
                            .addSnapshotListener { fbSnap, fbErr ->
                                if (fbErr != null) {
                                    close(fbErr)
                                    return@addSnapshotListener
                                }
                                val rawList = fbSnap?.toObjects(Complaint::class.java) ?: emptyList()
                                trySend(rawList.sortedByDescending { it.dateSubmitted })
                            }
                    } else {
                        close(e)
                    }
                    return@addSnapshotListener
                }
                
                val complaints = snapshot?.toObjects(Complaint::class.java) ?: emptyList()
                trySend(complaints)
            }
        
        awaitClose { 
            indexedListener.remove()
            fallbackListener?.remove()
        }
    }

    override fun getAllComplaints(): Flow<List<Complaint>> = callbackFlow {
        val listener = firestore.collection("complaints")
            .orderBy("dateSubmitted", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }
                
                val complaints = snapshot?.toObjects(Complaint::class.java) ?: emptyList()
                trySend(complaints)
            }
        
        awaitClose { listener.remove() }
    }

    override suspend fun createComplaint(complaint: Complaint) {
        firestore.collection("complaints").add(complaint).await()
    }

    override suspend fun updateComplaintStatus(complaintId: String, status: String, reply: String?) {
        val updates = mutableMapOf<String, Any>("status" to status)
        if (reply != null) {
            updates["reply"] = reply
            updates["replyDate"] = java.util.Date()
        }
        firestore.collection("complaints").document(complaintId).update(updates).await()
    }
}
