package com.aliminder.app.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aliminder.app.domain.model.Event
import com.aliminder.app.presentation.mock.MockData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Event card showing title, time, status ring, and expandable PoNR math.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EventCard(
    event: Event,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .bringIntoViewRequester(bringIntoViewRequester)
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
        )
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
                // Status ring
                StatusRing(
                    stage = event.getPersonaStage(),
                    deltaText = MockData.formatDelta(event.delta.toLong()),
                    size = 60.dp,
                    strokeWidth = 5.dp
                )
                
                // Event details
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "Starts: ${MockData.formatTime(event.startTime)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = "PoNR: ${event.ponr?.ponrTime?.let { MockData.formatTime(it) } ?: "N/A"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Expandable PoNR math
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                PoNRMathCard(event = event)
            }
        }
    }
}
