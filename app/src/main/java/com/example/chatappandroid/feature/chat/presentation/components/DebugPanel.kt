package com.example.chatappandroid.feature.chat.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Red = Color(0xFFE53935)
private val RedContainer = Color(0xFFFFEBEE)
private val DebugBg = Color(0xFF1E1E2E)
private val DebugSurface = Color(0xFF2A2A3E)

@Composable
fun DebugPanel(
    isFailArmed: Boolean,
    onSimulateRapidInbound: () -> Unit,
    onToggleTyping: () -> Unit,
    onSimulateNextFailure: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(DebugBg)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Label chip
        Text(
            text = "DEBUG",
            color = Color(0xFF888AAA),
            fontSize = 9.sp,
            letterSpacing = 1.5.sp,
            modifier = Modifier
                .background(DebugSurface, RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 3.dp),
        )

        // Rapid Inbound
        Button(
            onClick = onSimulateRapidInbound,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Red,
                contentColor = Color.White,
            ),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 6.dp),
        ) {
            Text("⚡ Rapid", fontSize = 11.sp, maxLines = 1)
        }

        // Toggle Typing
        OutlinedButton(
            onClick = onToggleTyping,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFF90CAF9),
            ),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 6.dp),
        ) {
            Text("✎ Typing", fontSize = 11.sp, maxLines = 1, color = Color(0xFF90CAF9))
        }

        // Force Fail toggle
        if (isFailArmed) {
            Button(
                onClick = onSimulateNextFailure,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Red,
                    contentColor = Color.White,
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 6.dp),
            ) {
                Text("✕ Will Fail", fontSize = 11.sp, maxLines = 1)
            }
        } else {
            OutlinedButton(
                onClick = onSimulateNextFailure,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF888AAA),
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 6.dp),
            ) {
                Text("Force Fail", fontSize = 11.sp, maxLines = 1, color = Color(0xFF888AAA))
            }
        }
    }
}
