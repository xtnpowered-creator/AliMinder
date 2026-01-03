package com.aliminder.app.presentation.screens.filters

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aliminder.app.presentation.theme.aliMinderTopAppBarColors

/**
 * Filters Screen - Provider toggles and sorting options
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltersScreen() {
    var showShadow by remember { mutableStateOf(true) }
    var showM365 by remember { mutableStateOf(false) }
    var showGoogle by remember { mutableStateOf(false) }
    var sortByPoNR by remember { mutableStateOf(true) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Filters") },
                colors = aliMinderTopAppBarColors()
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(16.dp)
        ) {
            Text(
                text = "Calendar Providers",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Shadow Calendar toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Shadow Calendar", style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = showShadow,
                    onCheckedChange = { showShadow = it }
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Microsoft 365 toggle (disabled)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Microsoft 365", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = "Not yet configured",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = showM365,
                    onCheckedChange = { showM365 = it },
                    enabled = false
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Google Workspace toggle (disabled)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Google Workspace", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = "Not yet configured",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = showGoogle,
                    onCheckedChange = { showGoogle = it },
                    enabled = false
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            HorizontalDivider() // Fixed: Divider -> HorizontalDivider
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Sorting",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Sort by PoNR proximity
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "By PoNR Proximity", style = MaterialTheme.typography.bodyLarge)
                RadioButton(
                    selected = sortByPoNR,
                    onClick = { sortByPoNR = true }
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Sort by start time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "By Start Time", style = MaterialTheme.typography.bodyLarge)
                RadioButton(
                    selected = !sortByPoNR,
                    onClick = { sortByPoNR = false }
                )
            }
        }
    }
}
