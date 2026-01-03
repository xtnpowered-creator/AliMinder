package com.aliminder.app.presentation.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aliminder.app.data.service.GoogleMapsTravelTimeService
import kotlinx.coroutines.launch

/**
 * Test screen for Google Maps API integration.
 * Shows a button to test travel time calculation.
 */
@Composable
fun ApiTestScreen(
    travelTimeService: GoogleMapsTravelTimeService = GoogleMapsTravelTimeService()
) {
    var testResult by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Google Maps API Test",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            "This test calculates travel time from a sample origin to destination.",
            style = MaterialTheme.typography.bodyMedium
        )

        Button(
            onClick = {
                isLoading = true
                testResult = null
                scope.launch {
                    try {
                        // Test: Calculate travel time from Seattle to San Francisco
                        val minutes = travelTimeService.calculateTravelTime(
                            origin = "Seattle, WA",
                            destination = "San Francisco, CA"
                        )

                        testResult = if (minutes != null) {
                            "✅ SUCCESS!\n\nTravel time: $minutes minutes\n\n" +
                                    "API is working correctly. Check Logcat for details."
                        } else {
                            "❌ FAILED\n\nAPI returned null. Check Logcat for error details."
                        }
                    } catch (e: Exception) {
                        testResult = "❌ ERROR\n\n${e.message}\n\nCheck Logcat for stack trace."
                    } finally {
                        isLoading = false
                    }
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Test API Call")
        }

        if (isLoading) {
            CircularProgressIndicator()
        }

        testResult?.let { result ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (result.contains("SUCCESS"))
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        result,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Expected Result: ~730-800 minutes (12-13 hours driving)\n\n" +
                    "If you see SUCCESS, the Google Maps Distance Matrix API is working!",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
