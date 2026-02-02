package com.example.zyberauto.presentation.secretary.bookings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.zyberauto.presentation.common.components.AppButton
import com.example.zyberauto.presentation.common.components.AppTextField

@Composable
fun RejectBookingDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var reason by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Decline Booking Request") },
        text = {
            Column {
                Text("Please provide a reason for declining this booking:")
                Spacer(modifier = Modifier.height(8.dp))
                AppTextField(
                    value = reason,
                    onValueChange = { 
                        reason = it
                        isError = false
                    },
                    label = "Reason (e.g. Parts unavailable)",
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    singleLine = false
                )
                if (isError) {
                    Text(
                        "Reason is required",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            AppButton(
                text = "Decline Booking",
                onClick = {
                    if (reason.isBlank()) {
                        isError = true
                    } else {
                        onConfirm(reason)
                    }
                },
                containerColor = MaterialTheme.colorScheme.error
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
