package com.gatalinka.app.ui.design

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.*
import com.gatalinka.app.ui.components.CoffeeBeanImageButton
import com.gatalinka.app.ui.theme.MysticGold
import com.gatalinka.app.ui.theme.MysticGoldDim
import com.gatalinka.app.ui.theme.MysticPurpleDeep
import com.gatalinka.app.ui.theme.MysticPurpleMedium
import com.gatalinka.app.ui.theme.MysticTextLight

object GataUI {
    // Boje - Standardizirane kroz cijeli app
    val Beige: Color = Color(0xFFFFE9C6)
    val MysticGold: Color = com.gatalinka.app.ui.theme.MysticGold
    val MysticGoldLight: Color = Color(0xFFFFE9C6) // Za tekst na tamnoj pozadini
    val MysticPurpleDeep: Color = com.gatalinka.app.ui.theme.MysticPurpleDeep
    val MysticPurpleMedium: Color = com.gatalinka.app.ui.theme.MysticPurpleMedium
    val MysticText: Color = com.gatalinka.app.ui.theme.MysticTextLight
    val MysticTextDim: Color = com.gatalinka.app.ui.theme.MysticTextLight.copy(alpha = 0.7f)
    val ErrorRed: Color = Color(0xFFFF6B6B)
    val SuccessGreen: Color = Color(0xFF4CAF50)

    // Dimenzije - WOW verzija s konzistentnim spacing-om
    val ScreenPadding = 24.dp
    val CardPadding = 20.dp
    val CardSpacing = 16.dp
    val SectionSpacing = 24.dp
    val BeanWidth = 260.dp
    val BeanHeight = 110.dp
    val GapAfterBean = 18.dp
    
    // Card dimensions
    val CardCornerRadius = 20.dp
    val SmallCardCornerRadius = 12.dp
    val LargeCardCornerRadius = 24.dp

    // Tekst
    val BeanLabelSize = 16.sp
    
    // Spacing constants - Standardizirani kroz app
    val SpacingXS = 4.dp
    val SpacingS = 8.dp
    val SpacingM = 16.dp
    val SpacingL = 24.dp
    val SpacingXL = 32.dp
    val SpacingXXL = 40.dp
    
    // Button dimensions
    val ButtonHeight = 56.dp
    val ButtonCornerRadius = 16.dp
}

@Composable
fun BeanCTA(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        CoffeeBeanImageButton(
            text = "",
            onClick = onClick,
            width = GataUI.BeanWidth,
            height = GataUI.BeanHeight,
            preciseHit = false,  // Omogući klik bilo gdje na gumbu
            enabled = enabled
        )
        Spacer(Modifier.height(GataUI.GapAfterBean))
        Text(
            text = label,
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = GataUI.BeanLabelSize,
                color = if (enabled) GataUI.Beige else GataUI.Beige.copy(alpha = 0.5f)
            )
        )
    }
}
