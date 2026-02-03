package com.example.zyberauto.presentation.secretary.appointments

import androidx.compose.runtime.Composable
import com.example.zyberauto.presentation.secretary.bookings.BookingRequestsScreen

@Composable
fun SecretaryScheduleWrapperScreen(
    onNavigateToSchedule: () -> Unit,
    onNavigateToWalkIn: () -> Unit,
    onNavigateToBookingDetails: (String) -> Unit
) {
    // User requested to consolidate Appointments/Requests into a single view 
    // using the BookingRequests UI style.
    BookingRequestsScreen(
        onNavigateToDetails = onNavigateToBookingDetails
    )
}
