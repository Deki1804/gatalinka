package com.gatalinka.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gatalinka.app.ui.design.GataUI

/**
 * Reusable empty state komponenta sa standardiziranim dizajnom
 */
@Composable
fun EmptyState(
    emoji: String = "📖",
    title: String,
    subtitle: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = GataUI.MysticPurpleMedium.copy(alpha = 0.5f)
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(GataUI.CardCornerRadius)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(GataUI.SpacingXL),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = emoji,
                fontSize = 64.sp,
                modifier = Modifier.padding(bottom = GataUI.SpacingM)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = GataUI.MysticText,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = GataUI.SpacingS)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = GataUI.MysticTextDim,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = if (actionLabel != null) GataUI.SpacingL else 0.dp)
            )
            
            if (actionLabel != null && onAction != null) {
                Button(
                    onClick = onAction,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GataUI.MysticGold,
                        contentColor = GataUI.MysticPurpleDeep
                    ),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(GataUI.ButtonCornerRadius),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(GataUI.ButtonHeight)
                ) {
                    Text(
                        text = actionLabel,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                }
            }
        }
    }
}

