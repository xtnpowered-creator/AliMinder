package com.aliminder.app.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.layout
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
import androidx.compose.ui.unit.sp
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
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp), // Tightened vertical padding
                verticalAlignment = Alignment.CenterVertically, 
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left Column: Squircle + Source Tag (The "Pedestal")
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val stage = duty.getPersonaStage()
                    val deltaValue = Duration.between(LocalDateTime.now(), duty.startTime).toMinutes()

                    StatusRing(
                        stage = stage,
                        deltaText = formatEventDelta(deltaValue, stage),
                        size = 48.dp, // Reduced to 48dp (Compact)
                        strokeWidth = 5.dp
                    )
                    
                    // Pedestal Text (Source Tag)
                    duty.sourceTag?.let { tag ->
                        Spacer(modifier = Modifier.height(2.dp)) // Reduced spacer
                        Text(
                            text = tag.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp, 
                            lineHeight = 10.sp, // Tight line height
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Bold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }

                // Right Column: Title + Time
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = duty.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    val isTask = duty.category?.contains("Task", ignoreCase = true) == true
                    val isToday = duty.startTime.toLocalDate() == java.time.LocalDate.now()
                    val daySuffix = if (!isToday) {
                         ", ${duty.startTime.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.US).uppercase()}."
                    } else ""
                    
                    val timeText = if (isTask) {
                        "Due: ${MockData.formatTime(duty.startTime)}"
                    } else {
                        "${MockData.formatTime(duty.startTime)} -- ${MockData.formatTime(duty.endTime)}"
                    } + daySuffix

                    Text(
                        text = timeText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Warning icon if duty needs attention (Overlay)
            if (duty.needsAttention()) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "Needs attention",
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 8.dp, bottom = 8.dp) // Corner padding
                        .size(20.dp) // Slightly smaller for corner placement
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
            String.format(Locale.US, "%d %s", days, dayLabel)
        } else {
            String.format(Locale.US, "%02d:%02d", hours, mins)
        }
    } else {
        "LATE"
    }
}
