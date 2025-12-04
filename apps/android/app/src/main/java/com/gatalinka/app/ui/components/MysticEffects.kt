package com.gatalinka.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.graphicsLayer
import com.gatalinka.app.ui.theme.MysticPurpleDeep
import com.gatalinka.app.ui.theme.MysticPurpleMedium
import kotlin.math.sin
import kotlin.math.cos
import kotlin.random.Random

@Composable
fun MysticBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "background_anim")
    
    // Animiraj offset za gradijent
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        MysticPurpleMedium,
                        MysticPurpleDeep
                    ),
                    center = Offset(500f + offset, 500f + offset),
                    radius = 1500f
                )
            )
    ) {
        // Dodaj zvijezde/iskrice
        Sparkles()
        
        // Sadržaj ekrana
        content()
        
        // Dim na dnu (overlay)
        SmokeEffect(modifier = Modifier.fillMaxSize())
    }
}

@Composable
fun Sparkles(particleCount: Int = 50) {
    val infiniteTransition = rememberInfiniteTransition(label = "sparkles")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    
    // Memoriziraj pozicije čestica - više čestica za WOW efekt
    val particles = remember {
        List(particleCount) {
            Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                speed = Random.nextFloat() * 0.3f + 0.05f,
                size = Random.nextFloat() * 4f + 1.5f,
                initialPhase = Random.nextFloat() * 2f * Math.PI.toFloat()
            )
        }
    }
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        
        particles.forEach { particle ->
            // Animirane čestice koje se kreću
            val currentY = (particle.y + time * particle.speed) % 1f
            val currentX = particle.x + sin(time * 0.5f + particle.initialPhase) * 0.15f
            val particleAlpha = alpha * (0.4f + 0.6f * sin(time * 2f + particle.initialPhase))
            
            // Glow effect around particles
            drawCircle(
                color = Color(0xFFFFD700).copy(alpha = particleAlpha * 0.3f),
                radius = particle.size * 2f,
                center = Offset(
                    (currentX.coerceIn(0f, 1f)) * width,
                    currentY * height
                )
            )
            
            // Main particle
            drawCircle(
                color = Color.White.copy(alpha = particleAlpha.coerceIn(0.2f, 1f)),
                radius = particle.size,
                center = Offset(
                    (currentX.coerceIn(0f, 1f)) * width,
                    currentY * height
                )
            )
        }
    }
}

data class Particle(
    val x: Float,
    val y: Float,
    val speed: Float,
    val size: Float,
    val initialPhase: Float
)

@Composable
fun SmokeEffect(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "smoke")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        
        // Crtaj "dim" kao seriju prozirnih krugova koji se miču sinusno
        val smokeColor = Color.Gray.copy(alpha = 0.1f)
        
        for (i in 0..5) {
            val xBase = width * (0.2f + 0.15f * i)
            val yBase = height * 0.9f
            
            val xOffset = sin(time + i) * 50f
            val yOffset = -((time * 50f + i * 100f) % (height * 0.5f))
            
            val radius = 50f + i * 20f + sin(time * 2) * 10f
            
            drawCircle(
                color = smokeColor,
                radius = radius,
                center = Offset(xBase + xOffset, yBase + yOffset)
            )
        }
    }
}

/**
 * Pulsirajući tekst - tekst koji se povećava i smanjuje
 */
@Composable
fun PulsingText(
    text: String,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = androidx.compose.ui.text.TextStyle(),
    minScale: Float = 0.95f,
    maxScale: Float = 1.05f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "text_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = minScale,
        targetValue = maxScale,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    androidx.compose.material3.Text(
        text = text,
        style = style,
        modifier = modifier.scale(scale)
    )
}

/**
 * Glowing effect - pulsirajući glow oko elementa
 */
@Composable
fun GlowEffect(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFFFD700), // Mystic Gold
    durationMillis: Int = 1500
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow_effect")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Box(
        modifier = modifier
            .drawBehind {
                drawCircle(
                    color = color.copy(alpha = glowAlpha),
                    radius = size.minDimension / 2f
                )
            }
    )
}

/**
 * Reading Glow Effect - specijalni efekt za čitanje (kao da AI gleda u šalicu)
 * Pulsirajući koncentrični krugovi oko centra - WOW verzija
 */
@Composable
fun ReadingGlowEffect(
    modifier: Modifier = Modifier,
    centerX: Float? = null,
    centerY: Float? = null,
    color: Color = Color(0xFFFFD700).copy(alpha = 0.4f)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "reading_glow")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )

    Canvas(modifier = modifier) {
        val center = Offset(
            centerX ?: (size.width / 2f),
            centerY ?: (size.height / 2f)
        )
        
        // Crtaj 5 koncentričnih krugova koji pulsiraju (više za WOW efekt)
        for (i in 0..4) {
            val baseRadius = 150f + i * 80f
            val radius = baseRadius * (0.4f + pulse * 0.6f)
            val alpha = (1f - pulse) * (0.4f - i * 0.08f)
            
            // Glow effect
            drawCircle(
                color = color.copy(alpha = alpha.coerceIn(0f, 0.4f)),
                radius = radius * 1.2f,
                center = center
            )
            
            // Main circle
            drawCircle(
                color = color.copy(alpha = alpha.coerceIn(0f, 0.5f)),
                radius = radius,
                center = center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
            )
        }
    }
}

/**
 * Ripple Effect - valovi koji se šire od centra
 */
@Composable
fun RippleEffect(
    modifier: Modifier = Modifier,
    centerX: Float? = null,
    centerY: Float? = null,
    color: Color = Color.White.copy(alpha = 0.2f),
    rippleCount: Int = 3
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ripple")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple_progress"
    )

    Canvas(modifier = modifier) {
        val center = Offset(
            centerX ?: (size.width / 2f),
            centerY ?: (size.height / 2f)
        )
        val maxRadius = size.maxDimension / 2f

        for (i in 0 until rippleCount) {
            val rippleProgress = (progress + i * (1f / rippleCount)) % 1f
            val radius = maxRadius * rippleProgress
            val alpha = (1f - rippleProgress) * 0.3f

            drawCircle(
                color = color.copy(alpha = alpha),
                radius = radius,
                center = center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
            )
        }
    }
}

/**
 * Mystic Orb - floating orb koji se kreće po ekranu
 */
@Composable
fun MysticOrb(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFFFD700).copy(alpha = 0.4f),
    size: Float = 60f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orb_time"
    )

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb_pulse"
    )

    Canvas(modifier = modifier) {
        val centerX = this.size.width * (0.3f + 0.4f * sin(time))
        val centerY = this.size.height * (0.3f + 0.4f * cos(time * 0.7f))
        val baseRadius = minOf(this.size.width, this.size.height) * 0.1f
        val orbRadius = baseRadius * pulse

        // Glow
        drawCircle(
            color = color.copy(alpha = 0.2f),
            radius = orbRadius * 1.5f,
            center = Offset(centerX, centerY)
        )
        
        // Orb
        drawCircle(
            color = color,
            radius = orbRadius,
            center = Offset(centerX, centerY)
        )
    }
}
