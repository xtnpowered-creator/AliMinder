package com.aliminder.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.aliminder.app.domain.model.DismissalReason
import com.aliminder.app.domain.model.Duty

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DismissalDialog(
    duty: Duty,
    onDismissRequest: () -> Unit,
    onConfirm: (DismissalReason) -> Unit
) {
    val isTask = duty.category?.contains("Task", ignoreCase = true) == true
    
    // Determine buttons based on type
    val completedText = if (isTask) "I did it" else "I made it"
    val cancelledText = if (isTask) "Not doing it" else "Not going"
    
    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large,
            tonalElevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Remove '${duty.title}'?",
                    style = MaterialTheme.typography.headlineSmall
                )
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Completed Button
                    Button(
                        onClick = { onConfirm(DismissalReason.COMPLETED) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Text(completedText)
                    }
                    
                    // Cancelled Button
                    Button(
                        onClick = { onConfirm(DismissalReason.CANCELLED) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Text(cancelledText)
                    }
                    
                    // Hide Button
                    Button(
                        onClick = { onConfirm(DismissalReason.USER_HIDDEN) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Text("Just hide it")
                    }
                    
                    // Nevermind Button
                    OutlinedButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Nevermind")
                    }
                }
            }
        }
    }
}
