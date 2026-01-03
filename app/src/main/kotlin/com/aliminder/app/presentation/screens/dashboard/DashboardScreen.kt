package com.aliminder.app.presentation.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aliminder.app.presentation.components.AliMinderTopAppBar

/**
 * Dashboard screen showing KPI cards and quick overview of duties
 */
@Composable
fun DashboardScreen() {
    Scaffold(
        topBar = {
            AliMinderTopAppBar(
                title = "Dashboard",
                overallStage = com.aliminder.app.domain.model.PersonaStage.OPTIMISTIC,
                useDynamicColor = true
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Placeholder KPI cards - to be implemented
            item {
                KpiCard(
                    title = "Urgent Duties",
                    value = "2",
                    description = "Need attention NOW"
                )
            }
            
            item {
                KpiCard(
                    title = "Next PoNR",
                    value = "23 min",
                    description = "Leave for \"Dentist Appt\""
                )
            }
            
            item {
                KpiCard(
                    title = "Today's Progress",
                    value = "3 / 8",
                    description = "Completed duties"
                )
            }
        }
    }
}

/**
 * Simple KPI card component (placeholder for full implementation)
 */
@Composable
private fun KpiCard(
    title: String,
    value: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
