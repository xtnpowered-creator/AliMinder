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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import com.aliminder.app.domain.model.getAttentionReason
import com.aliminder.app.domain.model.needsAttention
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
    onSetCommute: (String, Int) -> Unit = { _, _ -> }, // dutyId, minutes - default no-op
    onDismissAttention: (String) -> Unit = { }, // dutyId - default no-op
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var showCommuteDialog by remember { mutableStateOf(false) }
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()
    
    val cardShape = CardDefaults.shape

    Card(
        modifier = modifier
            .fillMaxWidth()
            .bringIntoViewRequester(bringIntoViewRequester)
            .clip(cardShape) 
            .clickable { 
                expanded = !expanded 
                if (expanded) {
                    coroutineScope.launch {
                        delay(300) 
                        bringIntoViewRequester.bringIntoView()
                    }
                }
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
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
                val stage = duty.getPersonaStage()
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
                    
                    val isTask = duty.category?.contains("Task", ignoreCase = true) == true
                    val timeLabel = if (isTask) "Due" else "Start"

                    Text(
                        text = "$timeLabel: ${MockData.formatTime(duty.startTime)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (stage != PersonaStage.URGENT && stage != PersonaStage.LATE) {
                        Text(
                            text = "PoNR: ${duty.ponr?.ponrTime?.let { MockData.formatTime(it) } ?: "N/A"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Warning icon if duty needs attention
                if (duty.needsAttention()) {
                    Icon(
                        imageVector = Icons.Outlined.Warning,
                        contentDescription = "Needs attention",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            // Expandable PoNR math
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    PoNRMathCard(duty = duty)
                    
                    // Attention section if needed
                    duty.getAttentionReason()?.let { reason ->
                        Spacer(modifier = Modifier.height(12.dp))
                        AttentionSection(
                            reason = reason,
                            onSetCommute = { showCommuteDialog = true },
                            onDismiss = { onDismissAttention(duty.id) }
                        )
                    }
                }
            }
        }
    }
    
    // Show commute dialog
    if (showCommuteDialog) {
        SetCommuteDialog(
            dutyTitle = duty.title,
            onDismiss = { showCommuteDialog = false },
            onSave = { minutes ->
                onSetCommute(duty.id, minutes)
                showCommuteDialog = false
            }
        )
    }
}

/**
 * Formats the delta text based on the urgency stage.
 */
private fun formatEventDelta(minutes: Long, stage: PersonaStage): String {
    val absMinutes = abs(minutes)
    val days = absMinutes / (24 * 60)
    val remainingMinutes = absMinutes % (24 * 60)
    val hours = remainingMinutes / 60
    val mins = remainingMinutes % 60

    if (stage == PersonaStage.LATE) {
        return "LATE"
    }
    
    if (stage == PersonaStage.URGENT) {
        return String.format(Locale.US, "%02d:%02d", hours, mins)
    }

    return if (minutes >= 0) {
        if (days > 0) {
            val dayLabel = if (days == 1L) "day" else "days"
            String.format(Locale.US, "%d %s\n%02d:%02d", days, dayLabel, hours, mins)
        } else {
            String.format(Locale.US, "%02d:%02d", hours, mins)
        }
    } else {
        "LATE"
    }
}
