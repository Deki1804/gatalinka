package com.gatalinka.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gatalinka.app.R

// add on top of the file (or below imports)
enum class BeanLabelPlacement { Overlay, Below }

@Composable
fun CoffeeBeanImageButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    width: Dp = 320.dp,
    height: Dp = 120.dp,
    preciseHit: Boolean = true,
    labelColor: Color = Color.White,
    labelSizeSp: Int = 20,
    labelPlacement: BeanLabelPlacement = BeanLabelPlacement.Overlay,
    enabled: Boolean = true
) {
    // Mystic Eye Button Design
    val infiniteTransition = rememberInfiniteTransition(label = "eye_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    androidx.compose.foundation.layout.Column(
        modifier = modifier
            .size(width, height + 30.dp), // Extra space for glow
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(width, height)
                .alpha(if (enabled) 1f else 0.5f)
                .pointerInput(enabled) {
                    if (enabled) {
                        detectTapGestures(onTap = { onClick() })
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2
                val cy = size.height / 2
                val radius = size.height / 2 * 0.9f * pulseScale
                
                // 1. Outer Glow
                drawCircle(
                    brush = androidx.compose.ui.graphics.Brush.radialGradient(
                        colors = listOf(
                            com.gatalinka.app.ui.theme.MysticGold.copy(alpha = 0.6f),
                            Color.Transparent
                        ),
                        center = Offset(cx, cy),
                        radius = radius * 1.5f
                    ),
                    radius = radius * 1.5f,
                    center = Offset(cx, cy)
                )

                // 2. Main Circle (The Eye/Ball)
                drawCircle(
                    brush = androidx.compose.ui.graphics.Brush.radialGradient(
                        colors = listOf(
                            com.gatalinka.app.ui.theme.MysticPurpleMedium,
                            com.gatalinka.app.ui.theme.MysticPurpleDeep
                        ),
                        center = Offset(cx - radius * 0.3f, cy - radius * 0.3f),
                        radius = radius
                    ),
                    radius = radius,
                    center = Offset(cx, cy)
                )
                
                // 3. Inner Iris/Pupil
                drawCircle(
                    color = com.gatalinka.app.ui.theme.MysticGold,
                    radius = radius * 0.4f,
                    center = Offset(cx, cy),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4.dp.toPx())
                )
                
                drawCircle(
                    color = com.gatalinka.app.ui.theme.MysticGold.copy(alpha = 0.8f),
                    radius = radius * 0.15f,
                    center = Offset(cx, cy)
                )
            }

            if (labelPlacement == BeanLabelPlacement.Overlay) {
                Text(
                    text = text,
                    style = TextStyle(
                        color = labelColor, 
                        fontSize = labelSizeSp.sp, 
                        fontWeight = FontWeight.Bold,
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = Color.Black,
                            blurRadius = 4f
                        )
                    )
                )
            }
        }
        
        if (labelPlacement == BeanLabelPlacement.Below) {
            Spacer(Modifier.padding(top = 8.dp))
            Text(
                text = text,
                style = TextStyle(color = labelColor, fontSize = labelSizeSp.sp, fontWeight = FontWeight.SemiBold)
            )
        }
    }
}
