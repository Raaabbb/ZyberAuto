package com.example.zyberauto.domain.repository

import com.example.zyberauto.domain.model.Inquiry
import kotlinx.coroutines.flow.Flow

interface InquiriesRepository {
    fun getInquiriesForUser(userId: String): Flow<List<Inquiry>>
    fun getAllInquiries(): Flow<List<Inquiry>> // For Secretary
    suspend fun createInquiry(inquiry: Inquiry)
    suspend fun updateInquiryStatus(inquiryId: String, status: String, reply: String?)
}
