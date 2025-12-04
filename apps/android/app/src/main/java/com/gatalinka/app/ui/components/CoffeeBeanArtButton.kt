package com.gatalinka.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Back-compat wrapper: ekrani i dalje zovu CoffeeBeanArtButton,
 * a ispod se koristi slika s alfom.
 */
@Composable
fun CoffeeBeanArtButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    width: Dp = 320.dp,
    height: Dp = 120.dp,
    stretchX: Float = 1.2f,
    showSteam: Boolean = false,            // ignoriramo (slika)
    preciseHit: Boolean = true,
    // NOVO:
    labelColor: Color = Color(0xFFEAD6BE), // bež kao naslov
    labelSizeSp: Int = 18,
    labelPlacement: BeanLabelPlacement = BeanLabelPlacement.Below
) {
    CoffeeBeanImageButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        width = width * stretchX,
        height = height,
        preciseHit = preciseHit,
        labelColor = labelColor,
        labelSizeSp = labelSizeSp,
        labelPlacement = labelPlacement
    )
}

