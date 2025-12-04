package com.gatalinka.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Typography
import androidx.compose.foundation.shape.RoundedCornerShape

private val CoffeeDark = darkColorScheme(
    primary = MysticGold,
    onPrimary = Color.Black,
    secondary = MysticGoldDim,
    onSecondary = Color.Black,
    background = MysticPurpleDeep,
    onBackground = MysticTextLight,
    surface = MysticPurpleMedium,
    onSurface = MysticTextLight,
    surfaceVariant = MysticPurpleMedium,
    onSurfaceVariant = MysticTextLight
)

val BeanShapes = Shapes(
    extraSmall = RoundedCornerShape(10),
    small = RoundedCornerShape(14),
    medium = RoundedCornerShape(18),
    large = RoundedCornerShape(50),      // pill/bean
    extraLarge = RoundedCornerShape(50)
)

@Composable
fun GatalinkaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CoffeeDark,
        typography = com.gatalinka.app.ui.theme.Typography,
        shapes = BeanShapes,
        content = content
    )
}

