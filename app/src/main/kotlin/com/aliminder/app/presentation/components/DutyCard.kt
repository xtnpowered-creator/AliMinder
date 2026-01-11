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
            // Shared Header (Squircle + Title + Time)
            DutyCardHeader(
                duty = duty,
                modifier = Modifier
            )

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


