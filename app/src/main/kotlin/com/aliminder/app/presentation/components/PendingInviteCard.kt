package com.aliminder.app.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Attention card for pending duty invites/assignments.
 * Shows Accept/Deny buttons.
 */
@Composable
fun PendingInviteCard(
    onAccept: () -> Unit,
    onDeny: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A1A) // Black background matching LocationSuggestionCard
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "Invited/Assigned to this duty.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Accept and Deny buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f),
                    border = BorderStroke(1.dp, Color(0xFF4CAF50)), // Green border
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("Accept", color = Color(0xFF4CAF50))
                }
                
                OutlinedButton(
                    onClick = onDeny,
                    modifier = Modifier.weight(1f),
                    border = BorderStroke(1.dp, Color(0xFFF44336)), // Red border
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("Deny", color = Color(0xFFF44336))
                }
            }
        }
    }
}
