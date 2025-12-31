package com.aliminder.app.presentation.screens.singroup

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aliminder.app.presentation.mock.MockData
import com.aliminder.app.presentation.mock.MockSinGroupApp

/**
 * Sin Group Screen - Blacklisted app management
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SinGroupScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sin Group") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* TODO: Add app */ }) {
                Icon(Icons.Default.Add, contentDescription = "Add app")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Blacklisted Apps",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Apps that trigger interventions when active",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            items(MockData.sampleSinGroupApps) { app ->
                SinGroupAppCard(app = app)
            }
        }
    }
}

@Composable
fun SinGroupAppCard(app: MockSinGroupApp) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Grace: ${app.gracePeriodSeconds}s",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = when (app.energyLevel) {
                        "High" -> MaterialTheme.colorScheme.errorContainer
                        "Medium" -> MaterialTheme.colorScheme.secondaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "${app.energyLevel} Energy",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            IconButton(onClick = { /* TODO: Delete */ }) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remove app",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
