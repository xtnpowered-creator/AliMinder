package com.aliminder.app.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aliminder.app.domain.model.Duty
import com.aliminder.app.domain.model.PersonaStage
import com.aliminder.app.domain.model.PoNRDataQuality
import com.aliminder.app.presentation.mock.MockData
import com.aliminder.app.presentation.theme.LateRed
import com.aliminder.app.presentation.theme.OptimisticGreen
import com.aliminder.app.presentation.theme.UrgentOrange
import com.aliminder.app.presentation.theme.WearyYellow

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
        
        // Travel, Buffer logic - use actual values from PoNR calculation
        val travel = duty.ponr?.commuteMinutes ?: 0
        val buffer = duty.ponr?.bufferMinutes ?: 10
        val quality = duty.ponr?.dataQuality ?: PoNRDataQuality.GOOD
        
        MathRow(
            label = "- Travel", 
            value = "$travel min",
            quality = if (!isTask) quality else PoNRDataQuality.GOOD // Only warn for travel-reliant events
        )
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
private fun MathRow(
    label: String, 
    value: String,
    quality: PoNRDataQuality = PoNRDataQuality.GOOD
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            if (quality != PoNRDataQuality.GOOD) {
                val (color, text) = when (quality) {
                    PoNRDataQuality.STALE -> WearyYellow to "STALE"
                    PoNRDataQuality.COARSE -> UrgentOrange to "COARSE"
                    else -> MaterialTheme.colorScheme.error to "ERROR"
                }
                
                Icon(
                    imageVector = Icons.Rounded.Warning,
                    contentDescription = text,
                    tint = color,
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .height(14.dp)
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelSmall,
                    color = color,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
