package com.aliminder.app.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Location suggestion card with inline Home/Work/Other buttons.
 * No popup needed for Home/Work if addresses are set.
 */
@Composable
fun LocationSuggestionCard(
    detectedKeyword: String,
    onSelectHome: () -> Unit,
    onSelectWork: () -> Unit,
    onSelectOther: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A1A) // Black background like duty cards
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "Task mentions \"$detectedKeyword\" but has no location. Add location?",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Three buttons inline with minimal padding
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onSelectHome,
                    modifier = Modifier.weight(1f),
                    border = BorderStroke(1.dp, Color.White),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("Home", color = Color.White)
                }
                
                OutlinedButton(
                    onClick = onSelectWork,
                    modifier = Modifier.weight(1f),
                    border = BorderStroke(1.dp, Color.White),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("Work", color = Color.White)
                }
                
                OutlinedButton(
                    onClick = onSelectOther,
                    modifier = Modifier.weight(1f),
                    border = BorderStroke(1.dp, Color.White),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("Other", color = Color.White)
                }
            }
        }
    }
}
