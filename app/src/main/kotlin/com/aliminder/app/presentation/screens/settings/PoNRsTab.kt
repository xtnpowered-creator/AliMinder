package com.aliminder.app.presentation.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aliminder.app.domain.model.UserSettings

@Composable
fun PoNRsTab(
    userSettings: UserSettings,
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    var defaultPrep by remember { mutableIntStateOf(userSettings.defaultPrepMinutes) }
    var defaultBuffer by remember { mutableIntStateOf(userSettings.defaultBufferMinutes) }
    var homeAddress by remember { mutableStateOf(userSettings.homeAddress ?: "") }
    var workAddress by remember { mutableStateOf(userSettings.workAddress ?: "") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Locations Section
        Text("Locations", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Set your common locations for quick assignment.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = homeAddress,
            onValueChange = { 
                homeAddress = it
                settingsViewModel.updateHomeAddress(it)
            },
            label = { Text("Home Address") },
            placeholder = { Text("123 Main St, City, State") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            maxLines = 2
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedTextField(
            value = workAddress,
            onValueChange = { 
                workAddress = it
                settingsViewModel.updateWorkAddress(it)
            },
            label = { Text("Work Address") },
            placeholder = { Text("456 Office Blvd, City, State") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            maxLines = 2
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(24.dp))
        
        // Default Parameters Section
        Text("Default Parameters", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Travel time is calculated automatically via Google Maps or set per-duty.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Default Prep Time", style = MaterialTheme.typography.bodyLarge)
            Text(text = "$defaultPrep min", style = MaterialTheme.typography.titleMedium)
        }
        Slider(
            value = defaultPrep.toFloat(),
            onValueChange = { defaultPrep = it.toInt() },
            valueRange = 0f..30f,
            steps = 5
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Default Buffer", style = MaterialTheme.typography.bodyLarge)
            Text(text = "$defaultBuffer min", style = MaterialTheme.typography.titleMedium)
        }
        Slider(
            value = defaultBuffer.toFloat(),
            onValueChange = { defaultBuffer = it.toInt() },
            valueRange = 0f..30f,
            steps = 5
        )
    }
}
