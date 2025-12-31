package com.aliminder.app.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aliminder.app.domain.model.Event
import com.aliminder.app.domain.model.PersonaStage
import com.aliminder.app.presentation.mock.MockData
import com.aliminder.app.presentation.theme.GraveRed
import com.aliminder.app.presentation.theme.OptimisticGreen
import com.aliminder.app.presentation.theme.WearyOrange

/**
 * Detailed breakdown of the PoNR calculation.
 * Formula: PoNR = Meeting - (Commute + Prep + Buffer)
 */
@Composable
fun PoNRMathCard(
    event: Event,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
    ) {
        Divider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "PoNR Calculation",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        // Math Rows
        MathRow("Meeting Time", MockData.formatTime(event.startTime))
        
        // Commute, Prep, Buffer logic
        // Use effective values (defaults are hardcoded 20/15/10 for now if null)
        // Ideally these defaults come from UserPreferences via ViewModel, but we'll hardcode for this display component
        val commute = event.getEffectiveCommuteMinutes(20)
        val prep = event.getEffectivePrepMinutes(15)
        val buffer = event.getEffectiveBufferMinutes(10)
        
        MathRow("- Commute", "$commute min")
        MathRow("- Prep", "$prep min")
        MathRow("- Buffer", "$buffer min")
        
        Divider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )
        
        // Result Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "= PoNR",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = event.ponr?.ponrTime?.let { MockData.formatTime(it) } ?: "N/A",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = when (event.getPersonaStage()) {
                    PersonaStage.OPTIMISTIC -> OptimisticGreen
                    PersonaStage.WEARY -> WearyOrange
                    PersonaStage.GRAVE -> GraveRed
                }
            )
        }
    }
}

@Composable
private fun MathRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}
