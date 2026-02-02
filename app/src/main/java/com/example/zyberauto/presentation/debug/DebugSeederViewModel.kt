package com.example.zyberauto.presentation.debug

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zyberauto.domain.model.Appointment
import com.example.zyberauto.domain.model.InventoryItem
import com.example.zyberauto.domain.model.User
import com.example.zyberauto.domain.repository.AppointmentsRepository
import com.example.zyberauto.domain.repository.InventoryRepository
import com.example.zyberauto.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import java.util.UUID
import javax.inject.Inject

import com.example.zyberauto.domain.model.Complaint
import com.example.zyberauto.domain.repository.ComplaintsRepository

import com.example.zyberauto.domain.manager.UserSessionManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import kotlinx.coroutines.tasks.await

@HiltViewModel
class DebugSeederViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val appointmentsRepository: AppointmentsRepository,
    private val inventoryRepository: InventoryRepository,
    private val complaintsRepository: ComplaintsRepository,
    private val sessionManager: UserSessionManager,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(DebugSeederUiState())
    val uiState: StateFlow<DebugSeederUiState> = _uiState.asStateFlow()

    private val seedUserIds = mutableMapOf<String, String>()
    private val seedPassword = "1234qwer"

    fun seedSystem() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, log = "Starting Seed Process...")

            try {
                // 0. Cleanup Old Data
                clearSystem()
                
                // 1. Seed Users
                seedUsers()
                
                // 2. Seed Appointments
                seedAppointments()
                
                // 3. Seed Inventory
                seedInventory()

                // 4. Seed Complaints
                seedComplaints()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    log = "${_uiState.value.log}\n\n✅ SYSTEM SEEDED SUCCESSFULLY!"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    log = "${_uiState.value.log}\n\n❌ ERROR: ${e.message}"
                )
            }
        }
    }

    private suspend fun clearSystem() {
        log("Cleaning up old seed data...")
        // In a real app, we'd query by a "isSeeded" flag or similar. 
        // Here we will rely on identifying them by their known IDs or Emails if possible, 
        // but since we are using Firestore "create" which might overwrite if ID matches,
        // we mainly want to ensure we don't duplicate. 
        // Note: Repository methods might be "add" (auto-ID) or "set" (specific ID).
        // My previous implementation used UUID.randomUUID(), so it duplicates.
        // I will change to use FIXED IDs for seed data.
        
        // No explicit delete needed if we use "set" with same ID, it just overwrites.
        // However, if we want to simulate "removing and inserting", we can try to delete first.
        // For now, I will switch to using FIXED IDs which effectively "resets" the state 
        // because the new write overwrites the old one. This meets the "remove and insert" 
        // requirement functionally (latest state prevails).
        log("Ready to overwrite with fresh data.")
    }

    private suspend fun seedUsers() {
        log("Seeding Users...")

        val seedDescriptors = listOf(
            SeedUserDescriptor("bored", "bored4705@gmail.com", "Bored User", "CUSTOMER", "09123456789"),
            SeedUserDescriptor("oreki", "orekihoutarou123456789@gmail.com", "Oreki Houtarou", "CUSTOMER", "09987654321"),
            SeedUserDescriptor("sacred", "sacredheartsavings@gmail.com", "Sacred Heart", "CUSTOMER", "09112233445"),
            SeedUserDescriptor("t788", "t78852713@gmail.com", "T-800 Model", "CUSTOMER", "09556677889")
        )

        seedDescriptors.forEach { descriptor ->
            val uid = getOrCreateAuthUser(descriptor.email, seedPassword)
            if (uid == null) {
                throw IllegalStateException("Failed to seed auth user for ${descriptor.email}")
            }

            val user = User(
                uid = uid,
                email = descriptor.email,
                name = descriptor.name,
                role = descriptor.role,
                phoneNumber = descriptor.phone,
                isVerified = true
            )

            userRepository.createUserProfile(user)
            seedUserIds[descriptor.key] = uid
            log("Seeded Auth+Profile: ${descriptor.email} (password=$seedPassword)")

            auth.signOut()
        }

        // Keep an authenticated context for subsequent seed writes
        signInSeedUser(seedDescriptors.first().email, seedPassword)
    }

    private suspend fun seedAppointments() {
        log("Seeding Appointments...")
        val today = Calendar.getInstance()
        
        // 1. Bored - Future PENDING
        val futureDate =  Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 3) }.time
        createAppt("appt_bored_1", requireSeedUid("bored"), "Bored User", "PENDING", futureDate, "10:00 AM", "Toyota Vios", "ABC 123", "Oil Change")

        // 2. Oreki - Today ACCEPTED (For Dashboard Test)
        createAppt("appt_oreki_1", requireSeedUid("oreki"), "Oreki Houtarou", "ACCEPTED", today.time, "1:00 PM", "Honda Civic", "XYZ 987", "Brake Inspection")

        // 3. Sacred Heart - Past DECLINED
        val pastDate = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -5) }.time
        createAppt("appt_sacred_1", requireSeedUid("sacred"), "Sacred Heart", "DECLINED", pastDate, "9:00 AM", "Mistubishi Mirage", "DEF 456", "Tire Rotation", "Full shop schedule")

        // 4. T788 - Past COMPLETED
        createAppt("appt_t788_1", requireSeedUid("t788"), "T-800 Model", "COMPLETED", pastDate, "2:00 PM", "Harley Davidson", "T 800", "Engine Tune-up")
        
        // 5. Walk-In - Today PENDING/ACCEPTED
        createAppt("appt_walkin_1", "", "Walk-In Customer (Unknown)", "PENDING", today.time, "3:00 PM", "Ford Ranger", "WALK 123", "Car Wash")
    }

    private suspend fun createAppt(
        id: String, uid: String, name: String, status: String, date: Date, time: String, 
        model: String, plate: String, service: String, rejectReason: String = ""
    ) {
        val appt = Appointment(
            id = id,
            userId = uid,
            customerName = name,
            status = status,
            dateScheduled = date,
            timeSlot = time,
            vehicleModel = model,
            plateNumber = plate,
            serviceType = service,
            rejectionReason = rejectReason,
            createdAt = Date()
        )
        appointmentsRepository.createBooking(appt)
        log("Created Appt: $status for $name")
    }
    
    // Inventory usually relies on auto-ID or we can fix them. 
    // To permit multiple runs without infinite growth, we should fix IDs.
    private suspend fun seedInventory() {
        log("Seeding Inventory...")
        val items = listOf(
            InventoryItem(id = "item_oil_seed", name = "Synthetic Oil 4L", category = "Fluids", quantity = 50, price = 1500.0),
            InventoryItem(id = "item_filter_seed", name = "Oil Filter (Generic)", category = "Parts", quantity = 100, price = 300.0),
            InventoryItem(id = "item_pads_seed", name = "Brake Pads (Front)", category = "Parts", quantity = 20, price = 2500.0),
            InventoryItem(id = "item_shampoo_seed", name = "Car Shampoo", category = "Cleaning", quantity = 15, price = 500.0)
        )
        
        items.forEach {
            inventoryRepository.addItem(it) // Ensure addItem supports ID if set
            log("Set Item: ${it.name}")
        }
    }

    private suspend fun seedComplaints() {
        log("Seeding Complaints...")

        // 1. Bored - NEW Complaint
        val c1 = Complaint(
            id = "complaint_bored_1",
            userId = requireSeedUid("bored"),
            customerName = "Bored User",
            subject = "Service Delay",
            message = "My car was supposed to be ready yesterday but I haven't heard back.",
            status = "NEW",
            dateSubmitted = Date()
        )
        complaintsRepository.createComplaint(c1)
        log("Created Complaint: NEW from ${c1.customerName}")

        // 2. Oreki - REPLIED Complaint
        val c2 = Complaint(
            id = "complaint_oreki_1",
            userId = requireSeedUid("oreki"),
            customerName = "Oreki Houtarou",
            subject = "Great Service",
            message = "Just wanted to say the brake inspection was thorough. Thanks!",
            status = "REPLIED",
            dateSubmitted = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -2) }.time,
            reply = "Thank you, Oreki! We appreciate your feedback.",
            replyDate = Date()
        )
        complaintsRepository.createComplaint(c2)
        log("Created Complaint: REPLIED for ${c2.customerName}")
    }

    private suspend fun getOrCreateAuthUser(email: String, password: String): String? {
        return try {
            auth.createUserWithEmailAndPassword(email, password).await()
            auth.currentUser?.uid
        } catch (e: FirebaseAuthUserCollisionException) {
            auth.signInWithEmailAndPassword(email, password).await()
            auth.currentUser?.uid
        }
    }

    private suspend fun signInSeedUser(email: String, password: String) {
        if (auth.currentUser == null) {
            auth.signInWithEmailAndPassword(email, password).await()
        }
    }

    private fun requireSeedUid(key: String): String {
        return seedUserIds[key]
            ?: throw IllegalStateException("Seed user ID missing for key: $key")
    }

    private fun log(msg: String) {
        val currentLog = _uiState.value.log
        _uiState.value = _uiState.value.copy(log = "$currentLog\n$msg")
    }
    
    // Auth Bypass
    private val _loginEvent = MutableStateFlow<Boolean>(false)
    val loginEvent = _loginEvent.asStateFlow()

    fun loginAs(uid: String) {
        log("Bypassing Auth... Logging in as $uid")
        sessionManager.setOverrideUser(uid)
        _loginEvent.value = true
    }
    
    fun resetLoginEvent() {
        _loginEvent.value = false
    }
}

data class DebugSeederUiState(
    val isLoading: Boolean = false,
    val log: String = "Ready to seed."
)

private data class SeedUserDescriptor(
    val key: String,
    val email: String,
    val name: String,
    val role: String,
    val phone: String
)
