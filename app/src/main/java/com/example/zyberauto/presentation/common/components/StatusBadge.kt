package com.example.zyberauto.presentation.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zyberauto.ui.theme.ErrorBgLight
import com.example.zyberauto.ui.theme.ErrorTextLight
import com.example.zyberauto.ui.theme.NeutralGray
import com.example.zyberauto.ui.theme.PrimaryRed
import com.example.zyberauto.ui.theme.SuccessBgLight
import com.example.zyberauto.ui.theme.SuccessTextLight
import com.example.zyberauto.ui.theme.WarningBgLight
import com.example.zyberauto.ui.theme.WarningTextLight

@Composable
fun StatusBadge(
    status: StatusType,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, contentColor) = when (status) {
        StatusType.Pending, StatusType.Processing -> WarningBgLight to WarningTextLight
        StatusType.Accepted, StatusType.Completed -> SuccessBgLight to SuccessTextLight
        StatusType.Rejected -> ErrorBgLight to ErrorTextLight
        StatusType.New -> PrimaryRed to Color.White
        StatusType.Replied, StatusType.Closed -> NeutralGray to Color.White
    }

    Box(
        modifier = modifier
            .background(color = backgroundColor, shape = RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = status.label,
            color = contentColor,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            fontSize = 10.sp
        )
    }
}

enum class StatusType(val label: String) {
    Pending("PENDING"),
    Accepted("ACCEPTED"),
    Rejected("REJECTED"),
    Processing("IN PROGRESS"),
    Completed("COMPLETED"),
    New("NEW"),
    Replied("REPLIED"),
    Closed("CLOSED")
}
