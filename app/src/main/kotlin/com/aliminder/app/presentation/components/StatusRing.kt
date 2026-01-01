package com.aliminder.app.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aliminder.app.domain.model.PersonaStage
import com.aliminder.app.presentation.theme.LateRed
import com.aliminder.app.presentation.theme.OptimisticGreen
import com.aliminder.app.presentation.theme.UrgentOrange
import com.aliminder.app.presentation.theme.WearyYellow

/**
 * Squircular status ring indicator showing persona stage and delta.
 */
@Composable
fun StatusRing(
    stage: PersonaStage,
    deltaText: String,
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    strokeWidth: Dp = 6.dp
) {
    val ringColor = when (stage) {
        PersonaStage.OPTIMISTIC -> OptimisticGreen
        PersonaStage.WEARY -> WearyYellow
        PersonaStage.URGENT -> UrgentOrange
        PersonaStage.LATE -> LateRed
    }

    val finalWidth = size * 1.32f // 10% wider than 1.2f (1.2 * 1.1)
    val finalHeight = size * 1.1f
    
    Box(
        modifier = modifier.size(width = finalWidth, height = finalHeight),
        contentAlignment = Alignment.Center // This will now work correctly
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidthPx = strokeWidth.toPx()
            
            val width = size.toPx() * 1.32f - strokeWidthPx
            val height = size.toPx() * 1.1f - strokeWidthPx

            val squircleFactor = 0.8f 
            val controlPointHorizontal = (width / 2f) * squircleFactor
            val controlPointVertical = (height / 2f) * squircleFactor

            val squirclePath = Path().apply {
                val cX = center.x
                val cY = center.y

                moveTo(cX, cY - height / 2f) // Start at Top-Center
                
                cubicTo(cX + controlPointHorizontal, cY - height / 2f, cX + width / 2f, cY - controlPointVertical, cX + width / 2f, cY)
                cubicTo(cX + width / 2f, cY + controlPointVertical, cX + controlPointHorizontal, cY + height / 2f, cX, cY + height / 2f)
                cubicTo(cX - controlPointHorizontal, cY + height / 2f, cX - width / 2f, cY + controlPointVertical, cX - width / 2f, cY)
                cubicTo(cX - width / 2f, cY - controlPointVertical, cX - controlPointHorizontal, cY - height / 2f, cX, cY - height / 2f)
                
                close()
            }
            
            drawPath(
                path = squirclePath,
                color = ringColor, // Use the direct color, no alpha animation
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )
        }
        
        // The Text composable is centered by the Box and handles its own multi-line alignment
        Text(
            text = deltaText,
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.2.sp), // Increased by 10% from 12.sp
            fontWeight = FontWeight.Bold,
            color = ringColor,
            textAlign = TextAlign.Center // Explicitly center multi-line text
        )
    }
}
