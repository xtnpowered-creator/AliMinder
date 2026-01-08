package com.aliminder.app.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.aliminder.app.domain.model.Address
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun AddressEntryModal(
    title: String,
    contextText: String? = null,
    initialAddress: Address? = null,
    suggestedName: String? = null,
    biasLocation: android.location.Location? = null,
    onSave: (Address) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    
    // Initialize Places if needed (safe to call multiple times)
    val placesClient = remember { Places.createClient(context) }
    val token = remember { AutocompleteSessionToken.newInstance() }
    val scope = rememberCoroutineScope()
    
    var step by remember { mutableStateOf(if (initialAddress != null) EntryStep.MANUAL else EntryStep.SEARCH) }
    
    // Form State
    var locationName by remember { mutableStateOf(initialAddress?.name ?: "") }
    var street by remember { mutableStateOf(initialAddress?.street ?: "") }
    var city by remember { mutableStateOf(initialAddress?.city ?: "") }
    var state by remember { mutableStateOf(initialAddress?.state ?: "") }
    var zipCode by remember { mutableStateOf(initialAddress?.zipCode ?: "") }
    
    // Search State
    var searchQuery by remember { mutableStateOf("") }
    var predictions by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                
                if (contextText != null) {
                    Text(
                        text = contextText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                when (step) {
                    EntryStep.SEARCH -> {
                        // Search Step
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { query ->
                                searchQuery = query
                                if (query.length > 2) {
                                    scope.launch {
                                        val requestBuilder = FindAutocompletePredictionsRequest.builder()
                                            .setSessionToken(token)
                                            .setQuery(query)

                                        // Apply Location Bias if available
                                        if (biasLocation != null) {
                                            val center = com.google.android.gms.maps.model.LatLng(biasLocation.latitude, biasLocation.longitude)
                                            // 5000 meters = 5km radius bias
                                            val bias = com.google.android.libraries.places.api.model.CircularBounds.newInstance(center, 5000.0)
                                            requestBuilder.setLocationBias(bias)
                                        }

                                        val request = requestBuilder.build()
                                            
                                        try {
                                            val response = placesClient.findAutocompletePredictions(request).await()
                                            predictions = response.autocompletePredictions.map {
                                                AutocompletePrediction(
                                                    placeId = it.placeId,
                                                    primaryText = it.getPrimaryText(null).toString(),
                                                    secondaryText = it.getSecondaryText(null).toString()
                                                )
                                            }
                                        } catch (e: Exception) {
                                            predictions = emptyList()
                                        }
                                    }
                                } else {
                                    predictions = emptyList()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Search for places or addresses") },
                            leadingIcon = { Icon(Icons.Default.Search, null) },
                            singleLine = true
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        LazyColumn(
                            modifier = Modifier.weight(1f)
                        ) {
                            items(predictions) { prediction ->
                                PredictionItem(
                                    prediction = prediction,
                                    onClick = { 
                                        // Fetch Place Details
                                        scope.launch {
                                            val placeFields = listOf(
                                                Place.Field.DISPLAY_NAME, 
                                                Place.Field.ADDRESS_COMPONENTS,
                                                Place.Field.FORMATTED_ADDRESS
                                            )
                                            val request = FetchPlaceRequest.newInstance(prediction.placeId, placeFields)
                                            
                                            try {
                                                val response = placesClient.fetchPlace(request).await()
                                                val place = response.place
                                                
                                                // Extract Address Components
                                                val components = place.addressComponents?.asList() ?: emptyList()
                                                
                                                fun getComponent(type: String, useShortName: Boolean = false): String {
                                                    val comp = components.find { it.types.contains(type) }
                                                    return if (useShortName) comp?.shortName ?: "" else comp?.name ?: ""
                                                }
                                                
                                                val number = getComponent("street_number")
                                                val route = getComponent("route")
                                                
                                                locationName = place.displayName ?: ""
                                                
                                                street = if (number.isNotBlank()) "$number $route" else route
                                                
                                                // Deduplicate Name/Street
                                                val isDuplicate = locationName.equals(street, ignoreCase = true) || locationName == place.formattedAddress

                                                
                                                if (isDuplicate) {
                                                    locationName = suggestedName ?: ""
                                                }
                                                
                                                city = getComponent("locality")
                                                if (city.isBlank()) city = getComponent("postal_town")
                                                
                                                state = getComponent("administrative_area_level_1", useShortName = true)
                                                
                                                zipCode = getComponent("postal_code")
                                                
                                                step = EntryStep.MANUAL
                                            } catch (e: Exception) {
                                                // Error handling
                                            }
                                        }
                                    }
                                )
                            }
                        }
                        
                        Button(
                            onClick = { step = EntryStep.MANUAL },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Enter Manually")
                        }
                    }
                    
                    EntryStep.MANUAL -> {
                        // Manual Entry / Verification Step
                        Column(
                            modifier = Modifier
                                .verticalScroll(rememberScrollState())
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedTextField(
                                value = locationName,
                                onValueChange = { locationName = it },
                                label = { Text("Location Name (Optional)") },
                                placeholder = { Text("e.g. Starbucks, Home") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            OutlinedTextField(
                                value = street,
                                onValueChange = { street = it },
                                label = { Text("Street Address") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            OutlinedTextField(
                                value = city,
                                onValueChange = { city = it },
                                label = { Text("City") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = state,
                                    onValueChange = { state = it.uppercase() },
                                    label = { Text("State (XX)") },
                                    modifier = Modifier.weight(1f)
                                )
                                
                                OutlinedTextField(
                                    value = zipCode,
                                    onValueChange = { zipCode = it },
                                    label = { Text("Zip Code") },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { step = EntryStep.SEARCH },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Back to Search")
                            }
                            
                            Button(
                                onClick = {
                                    onSave(Address(locationName.ifBlank { null }, street, city, state, zipCode))
                                },
                                modifier = Modifier.weight(1f),
                                enabled = street.isNotBlank() && city.isNotBlank() && state.isNotBlank()
                            ) {
                                Text("Save Location")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PredictionItem(
    prediction: AutocompletePrediction,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(prediction.primaryText, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text(prediction.secondaryText, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

data class AutocompletePrediction(
    val placeId: String,
    val primaryText: String,
    val secondaryText: String
)

enum class EntryStep {
    SEARCH,
    MANUAL
}
