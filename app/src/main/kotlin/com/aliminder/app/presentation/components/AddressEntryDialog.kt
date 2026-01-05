package com.aliminder.app.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.aliminder.app.domain.model.Address

/**
 * Dialog for entering a structured address.
 * Validates state (2-letter uppercase) and ZIP (5 digits).
 */
@Composable
fun AddressEntryDialog(
    title: String,
    contextText: String? = null,
    initialAddress: Address? = null,
    onSave: (Address) -> Unit,
    onDismiss: () -> Unit
) {
    var street by remember { mutableStateOf(initialAddress?.street ?: "") }
    var city by remember { mutableStateOf(initialAddress?.city ?: "") }
    var state by remember { mutableStateOf(initialAddress?.state ?: "") }
    var zipCode by remember { mutableStateOf(initialAddress?.zipCode ?: "") }
    
    var stateError by remember { mutableStateOf(false) }
    var zipError by remember { mutableStateOf(false) }
    
    // Validate on state change
    val isStateValid = state.isEmpty() || Address.isValidState(state)
    val isZipValid = zipCode.isEmpty() || Address.isValidZipCode(zipCode)
    val isFormValid = street.trim().isNotBlank() && 
                      city.trim().isNotBlank() && 
                      Address.isValidState(state) && 
                      Address.isValidZipCode(zipCode)
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(title, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (contextText != null) {
                    Text(
                        text = contextText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Street
                OutlinedTextField(
                    value = street,
                    onValueChange = { street = it },
                    label = { Text("Street Address") },
                    placeholder = { Text("123 Main St") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // City
                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("City") },
                    placeholder = { Text("Austin") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // State and ZIP on same row
                Row(modifier = Modifier.fillMaxWidth()) {
                    // State
                    OutlinedTextField(
                        value = state,
                        onValueChange = { 
                            // Auto-capitalize and limit to 2 chars
                            if (it.length <= 2) {
                                state = it.uppercase()
                                stateError = it.isNotEmpty() && !Address.isValidState(it.uppercase())
                            }
                        },
                        label = { Text("State") },
                        placeholder = { Text("TX") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        isError = stateError,
                        supportingText = if (stateError) {
                            { Text("2 letters", style = MaterialTheme.typography.bodySmall) }
                        } else null,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters
                        )
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    // ZIP Code
                    OutlinedTextField(
                        value = zipCode,
                        onValueChange = { 
                            // Limit to 5 digits
                            if (it.length <= 5 && it.all { char -> char.isDigit() }) {
                                zipCode = it
                                zipError = it.isNotEmpty() && !Address.isValidZipCode(it)
                            }
                        },
                        label = { Text("ZIP") },
                        placeholder = { Text("78701") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        isError = zipError,
                        supportingText = if (zipError) {
                            { Text("5 digits", style = MaterialTheme.typography.bodySmall) }
                        } else null,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val address = Address(
                        street = street.trim(),
                        city = city.trim(),
                        state = state.trim(),
                        zipCode = zipCode.trim()
                    )
                    onSave(address)
                },
                enabled = isFormValid
            ) {
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
