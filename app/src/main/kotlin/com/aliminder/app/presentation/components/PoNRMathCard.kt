package com.aliminder.app.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aliminder.app.domain.model.Duty
import com.aliminder.app.domain.model.PersonaStage
import com.aliminder.app.presentation.mock.MockData
import com.aliminder.app.presentation.theme.LateRed
import com.aliminder.app.presentation.theme.OptimisticGreen
import com.aliminder.app.presentation.theme.UrgentOrange
import com.aliminder.app.presentation.theme.WearyYellow

/**
 * Detailed breakdown of the PoNR calculation.
 * Formula: PoNR = StartTime - (Commute + Prep + Buffer)
 */
@Composable
fun PoNRMathCard(
    duty: Duty,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "PoNR Calculation",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        // Math Rows
        val isTask = duty.category?.contains("Task", ignoreCase = true) == true
        val timeLabel = if (isTask) "Due Time" else "Start Time"
        MathRow(timeLabel, MockData.formatTime(duty.startTime))
        
        // Commute, Prep, Buffer logic
        // Use effective values (defaults are hardcoded 20/15/10 for now if null)
        // Ideally these defaults come from UserPreferences via ViewModel, but we'll hardcode for this display component
        val commute = duty.getEffectiveCommuteMinutes(20)
        val prep = duty.getEffectivePrepMinutes(15)
        val buffer = duty.getEffectiveBufferMinutes(10)
        
        MathRow("- Commute", "$commute min")
        MathRow("- Prep", "$prep min")
        MathRow("- Buffer", "$buffer min")
        
        HorizontalDivider(
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
                text = duty.ponr?.ponrTime?.let { MockData.formatTime(it) } ?: "N/A",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = when (duty.getPersonaStage()) {
                    PersonaStage.OPTIMISTIC -> OptimisticGreen
                    PersonaStage.WEARY -> WearyYellow
                    PersonaStage.URGENT -> UrgentOrange
                    PersonaStage.LATE -> LateRed
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
