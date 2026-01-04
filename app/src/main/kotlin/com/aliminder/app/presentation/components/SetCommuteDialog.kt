package com.aliminder.app.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Dialog for setting custom travel time for a duty.
 */
@Composable
fun SetCommuteDialog(
    dutyTitle: String,
    onDismiss: () -> Unit,
    onSave: (Int) -> Unit
) {
    var minutes by remember { mutableIntStateOf(15) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text("Set Travel Time", fontWeight = FontWeight.Bold) 
        },
        text = { 
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "How long does it take to get to:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = dutyTitle,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "$minutes minutes",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Slider(
                    value = minutes.toFloat(),
                    onValueChange = { minutes = it.toInt() },
                    valueRange = 0f..120f,
                    steps = 23, // 5-minute increments
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text(
                    text = "0 min ←→ 120 min",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(minutes) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
