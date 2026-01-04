package com.aliminder.app.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Dialog for selecting location: Home, Work, or Other.
 * Handles logic for whether addresses are set or need entry.
 */
@Composable
fun SetLocationDialog(
    dutyTitle: String,
    homeAddress: String?,
    workAddress: String?,
    onUseAddress: (String) -> Unit,  // Called when using an existing address
    onEnterAddress: (addressType: String) -> Unit,  // Called when need to enter address ("Home", "Work", or "Other")
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Set Location", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Where is:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = dutyTitle,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Three buttons in a row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            if (!homeAddress.isNullOrBlank()) {
                                onUseAddress(homeAddress)
                            } else {
                                onEnterAddress("Home")
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Home")
                    }
                    
                    OutlinedButton(
                        onClick = {
                            if (!workAddress.isNullOrBlank()) {
                                onUseAddress(workAddress)
                            } else {
                                onEnterAddress("Work")
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Work")
                    }
                    
                    OutlinedButton(
                        onClick = { onEnterAddress("Other") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Other")
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
