package com.example.zyberauto.data.repository

import com.example.zyberauto.domain.model.Inquiry
import com.example.zyberauto.domain.repository.InquiriesRepository
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InquiriesRepositoryImpl @Inject constructor(
    private val database: FirebaseDatabase
) : InquiriesRepository {

    private val inquiriesRef = database.getReference("inquiries")

    override fun getInquiriesForUser(userId: String): Flow<List<Inquiry>> = callbackFlow {
        val query = inquiriesRef.orderByChild("userId").equalTo(userId)
        
        val listener = object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val inquiries = snapshot.children.mapNotNull { 
                    it.getValue(Inquiry::class.java)?.copy(id = it.key ?: "") 
                }.sortedByDescending { it.dateSubmitted }
                trySend(inquiries)
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                close(error.toException())
            }
        }
        query.addValueEventListener(listener)
        awaitClose { query.removeEventListener(listener) }
    }

    override fun getAllInquiries(): Flow<List<Inquiry>> = callbackFlow {
        val listener = object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val inquiries = snapshot.children.mapNotNull { 
                    it.getValue(Inquiry::class.java)?.copy(id = it.key ?: "") 
                }.sortedByDescending { it.dateSubmitted }
                trySend(inquiries)
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                close(error.toException())
            }
        }
        inquiriesRef.addValueEventListener(listener)
        awaitClose { inquiriesRef.removeEventListener(listener) }
    }

    override suspend fun createInquiry(inquiry: Inquiry) {
        val newRef = inquiriesRef.push()
        val inquiryWithId = inquiry.copy(id = newRef.key ?: "")
        newRef.setValue(inquiryWithId).await()
    }

    override suspend fun updateInquiryStatus(inquiryId: String, status: String, reply: String?) {
        val updates = mutableMapOf<String, Any>("status" to status)
        if (reply != null) {
            updates["reply"] = reply
            updates["replyDate"] = System.currentTimeMillis()
        }
        inquiriesRef.child(inquiryId).updateChildren(updates).await()
    }
}
