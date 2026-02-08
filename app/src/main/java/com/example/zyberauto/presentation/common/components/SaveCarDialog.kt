package com.example.zyberauto.presentation.common.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import com.example.zyberauto.ui.theme.PrimaryRed

/**
 * Dialog prompting user to save a manually entered vehicle to their profile.
 *
 * @param vehicleModel The vehicle model that was entered
 * @param plateNumber The plate number that was entered
 * @param onConfirm Called when user wants to save the car
 * @param onDismiss Called when user declines to save
 */
@Composable
fun SaveCarDialog(
    vehicleModel: String,
    plateNumber: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Save This Car?",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "Would you like to save \"$vehicleModel ($plateNumber)\" to your profile for faster booking next time?"
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed)
            ) {
                Text("Yes, Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("No, Thanks")
            }
        }
    )
}
