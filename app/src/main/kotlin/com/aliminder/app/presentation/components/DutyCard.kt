package com.aliminder.app.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aliminder.app.domain.model.Duty
import com.aliminder.app.domain.model.PersonaStage
import com.aliminder.app.presentation.mock.MockData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import java.util.Locale
import kotlin.math.abs

/**
 * Duty card showing title, time, status ring, and expandable PoNR math.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DutyCard(
    duty: Duty,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()
    
    // Define the shape explicitly to use for both Card and Clipping if needed
    val cardShape = CardDefaults.shape

    Card(
        modifier = modifier
            .fillMaxWidth()
            .bringIntoViewRequester(bringIntoViewRequester)
            // Clip the card content to its shape to prevent background bleed-through
            // when placed on top of colored swipe backgrounds.
            .clip(cardShape) 
            .clickable { 
                expanded = !expanded 
                if (expanded) {
                    coroutineScope.launch {
                        // Wait for the expand animation to likely complete (approx 300-500ms)
                        // so that the full height is available to be brought into view.
                        delay(300) 
                        bringIntoViewRequester.bringIntoView()
                    }
                }
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface // Explicitly set the background to SurfaceDark (black)
        ),
        shape = cardShape
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Main content row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Determine delta based on stage
                val stage = duty.getPersonaStage()
                // Always calculate delta relative to start/due time as requested for all stages
                val deltaValue = Duration.between(LocalDateTime.now(), duty.startTime).toMinutes()

                // Status ring
                StatusRing(
                    stage = stage,
                    deltaText = formatEventDelta(deltaValue, stage),
                    size = 60.dp,
                    strokeWidth = 5.dp
                )
                
                // Duty details
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = duty.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Determine label: "Due" for tasks, "Start" for events
                    val isTask = duty.category?.contains("Task", ignoreCase = true) == true
                    val timeLabel = if (isTask) "Due" else "Start"

                    Text(
                        text = "$timeLabel: ${MockData.formatTime(duty.startTime)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    // Only show PoNR time if not Urgent or Late
                    if (stage != PersonaStage.URGENT && stage != PersonaStage.LATE) {
                        Text(
                            text = "PoNR: ${duty.ponr?.ponrTime?.let { MockData.formatTime(it) } ?: "N/A"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Expandable PoNR math
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                PoNRMathCard(duty = duty)
            }
        }
    }
}

/**
 * Formats the delta text based on the urgency stage.
 * This logic is decoupled from MockData to ensure proper behavior in production.
 */
private fun formatEventDelta(minutes: Long, stage: PersonaStage): String {
    val absMinutes = abs(minutes)
    val days = absMinutes / (24 * 60)
    val remainingMinutes = absMinutes % (24 * 60)
    val hours = remainingMinutes / 60
    val mins = remainingMinutes % 60

    // Red Condition (LATE) -> Display "LATE" only
    if (stage == PersonaStage.LATE) {
        return "LATE"
    }
    
    // Orange Condition (URGENT) -> Display countdown timer only
    if (stage == PersonaStage.URGENT) {
        return String.format(Locale.US, "%02d:%02d", hours, mins)
    }

    // Green/Yellow (Optimistic/Weary) -> Standard formatting
    return if (minutes >= 0) {
        if (days > 0) {
            val dayLabel = if (days == 1L) "day" else "days"
            String.format(Locale.US, "%d %s\n%02d:%02d", days, dayLabel, hours, mins)
        } else {
            String.format(Locale.US, "%02d:%02d", hours, mins)
        }
    } else {
        // Fallback for negative numbers (should be caught by LATE, but just in case)
        "LATE"
    }
}
