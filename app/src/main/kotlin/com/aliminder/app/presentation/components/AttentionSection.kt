package com.aliminder.app.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.unit.dp

/**
 * Scrollable attention section with dashed white border and blue background.
 * Contains suggestion cards.
 */
@Composable
fun AttentionSection(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 200.dp)
            .border(
                BorderStroke(
                    width = 1.dp,
                    color = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ),
        color = Color(0xFF1E3A5F), // Dark blue matching other backgrounds
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            content()
        }
    }
}
