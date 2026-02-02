package com.example.zyberauto.domain.repository

import com.example.zyberauto.domain.model.Complaint
import kotlinx.coroutines.flow.Flow

interface ComplaintsRepository {
    fun getComplaintsForUser(userId: String): Flow<List<Complaint>>
    fun getAllComplaints(): Flow<List<Complaint>> // For Secretary
    suspend fun createComplaint(complaint: Complaint)
    suspend fun updateComplaintStatus(complaintId: String, status: String, reply: String?)
}
