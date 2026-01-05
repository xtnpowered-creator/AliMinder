package com.aliminder.app.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aliminder.app.domain.model.Duty
import com.aliminder.app.domain.model.PersonaStage
import com.aliminder.app.domain.model.needsAttention
import com.aliminder.app.presentation.mock.MockData
import java.time.Duration
import java.time.LocalDateTime
import java.util.Locale
import kotlin.math.abs

/**
 * Simplified duty card - click opens fullscreen modal.
 */
@Composable
fun DutyCard(
    duty: Duty,
    onCardClick: (Duty) -> Unit = { },
    modifier: Modifier = Modifier
) {
    val cardShape = CardDefaults.shape

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape) 
            .clickable { onCardClick(duty) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = cardShape
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val stage = duty.getPersonaStage()
            val deltaValue = Duration.between(LocalDateTime.now(), duty.startTime).toMinutes()

            StatusRing(
                stage = stage,
                deltaText = formatEventDelta(deltaValue, stage),
                size = 60.dp,
                strokeWidth = 5.dp
            )
            
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
            }
            
            // Warning icon if duty needs attention
            if (duty.needsAttention()) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "Needs attention",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

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
