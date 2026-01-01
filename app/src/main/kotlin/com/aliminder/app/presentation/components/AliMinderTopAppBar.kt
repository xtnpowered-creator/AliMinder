package com.aliminder.app.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aliminder.app.domain.model.PersonaStage
import com.aliminder.app.presentation.theme.BorderDark
import com.aliminder.app.presentation.theme.LateRed
import com.aliminder.app.presentation.theme.TextSecondary
import com.aliminder.app.presentation.theme.UrgentOrange
import com.aliminder.app.presentation.theme.WearyYellow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AliMinderTopAppBar(
    title: String,
    overallStage: PersonaStage?,
    useDynamicColor: Boolean
) {
    val topBarColor = if (useDynamicColor) {
        when (overallStage) {
            PersonaStage.WEARY -> WearyYellow
            PersonaStage.URGENT -> UrgentOrange
            PersonaStage.LATE -> LateRed
            else -> MaterialTheme.colorScheme.background
        }
    } else {
        MaterialTheme.colorScheme.background
    }

    Column {
        CenterAlignedTopAppBar(
            title = {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Text(
                        text = title,
                        fontSize = (MaterialTheme.typography.titleLarge.fontSize.value + 2).sp,
                        textAlign = TextAlign.Center,
                        color = if (topBarColor != MaterialTheme.colorScheme.background) Color.Black else TextSecondary
                    )
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = topBarColor
            )
        )
        HorizontalDivider(thickness = 2.dp, color = BorderDark)
    }
}
