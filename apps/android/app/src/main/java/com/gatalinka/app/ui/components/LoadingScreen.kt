package com.gatalinka.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gatalinka.app.ui.design.GataUI

/**
 * Reusable loading screen komponenta sa standardiziranim dizajnom
 */
@Composable
fun LoadingScreen(
    message: String = "Učitavam...",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(GataUI.ScreenPadding)
        ) {
            CircularProgressIndicator(
                color = GataUI.MysticGold,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(GataUI.SpacingM))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = GataUI.MysticText,
                textAlign = TextAlign.Center
            )
        }
    }
}

