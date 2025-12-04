package com.gatalinka.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import android.view.HapticFeedbackConstants
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.activity.compose.BackHandler
import com.gatalinka.app.ui.components.MysticBackground
import com.gatalinka.app.ui.components.ReadingGlowEffect
import com.gatalinka.app.ui.components.MysticOrb
import com.gatalinka.app.util.ErrorMessages
import com.gatalinka.app.ui.design.GataUI

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingResultScreen(
    result: com.gatalinka.app.ui.model.GatalinkaReadingUiModel,
    imageUri: String,
    onBack: () -> Unit,
    onSave: () -> Unit,
    targetName: String? = null // Ime osobe za koju je gatanje (null = za sebe)
) {
    val context = LocalContext.current
    val view = LocalView.current
    val coroutineScope = rememberCoroutineScope()
    val readingsRepo = remember { com.gatalinka.app.data.CloudReadingsRepository() }
    
    var isSaving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }
    
    // Presretni sistem back button i pozovi naš onBack callback
    BackHandler(onBack = onBack)
    
    // Share functionality
    fun buildShareText(result: com.gatalinka.app.ui.model.GatalinkaReadingUiModel): String {
        val sb = StringBuilder()
        sb.append("☕ Moje čitanje iz šalice kave - Gatalinka\n\n")
        sb.append("✨ Sreća: ${result.luckScore}/100\n")
        sb.append("⚡ Energija: ${result.energyScore}/100\n\n")
        
        if (result.mantra.isNotEmpty()) {
            sb.append("💫 ${result.mantra}\n\n")
        }
        
        sb.append("📖 ${result.mainText}\n\n")
        
        if (result.love?.isNotEmpty() == true) {
            sb.append("💕 Ljubav: ${result.love}\n\n")
        }
        if (result.work?.isNotEmpty() == true) {
            sb.append("💼 Posao: ${result.work}\n\n")
        }
        if (result.money?.isNotEmpty() == true) {
            sb.append("💰 Novac: ${result.money}\n\n")
        }
        if (result.health?.isNotEmpty() == true) {
            sb.append("🌿 Zdravlje: ${result.health}\n\n")
        }
        
        if (result.symbols.isNotEmpty()) {
            sb.append("🔮 Simboli: ${result.symbols.joinToString(", ")}\n\n")
        }
        
        if (result.luckyNumbers.isNotEmpty()) {
            sb.append("🎲 Sretni brojevi: ${result.luckyNumbers.joinToString(", ")}\n\n")
        }
        
        sb.append("Preuzmi Gatalinka app i otkrij svoju sudbinu! 🔮")
        return sb.toString()
    }
    
    fun shareReading() {
        val shareText = buildShareText(result)
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Dijeli čitanje")
        context.startActivity(shareIntent)
    }

    MysticBackground {
        // Additional glow effect behind header
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Subtle glow behind content
            ReadingGlowEffect(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.3f),
                centerX = null,
                centerY = null
            )
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
            // Top Bar
            TopAppBar(
                title = {
                    Column {
                        Text(
                            if (targetName != null) "Čitanje za: $targetName" else "Tvoja sudbina",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color(0xFFFFD700)
                        )
                        if (targetName != null) {
                            Text(
                                "Gatanje za drugu osobu",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFEFE3D1).copy(alpha = 0.7f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "Nazad",
                            tint = Color(0xFFEFE3D1)
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                            shareReading()
                        }
                    ) {
                        Icon(
                            Icons.Default.Share,
                            "Dijeli",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Podijeli",
                            color = Color(0xFFFFD700)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )

            // Content with entrance animation
            val entranceScale = remember { androidx.compose.animation.core.Animatable(0.9f) }
            LaunchedEffect(Unit) {
                entranceScale.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(800, easing = FastOutSlowInEasing)
                )
            }
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .navigationBarsPadding()
                    .padding(bottom = 100.dp) // Extra padding for bottom nav bar + button
                    .graphicsLayer {
                        scaleX = entranceScale.value
                        scaleY = entranceScale.value
                    }
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Stagger animations for cards - povećana lista za više kartica
                val cardDelays = listOf(0, 100, 200, 300, 400, 500, 600, 700, 800, 900, 1000, 1100)
                var cardIndex = 0
                
                // Helper funkcija za siguran pristup cardDelays
                fun getCardDelay(index: Int): Int {
                    return cardDelays.getOrElse(index) { cardDelays.last() }
                }
                
                // Luck Score Card with glassmorphism
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(durationMillis = 600, delayMillis = getCardDelay(cardIndex++))) +
                            slideInVertically(
                                initialOffsetY = { 30 },
                                animationSpec = tween(durationMillis = 600, delayMillis = getCardDelay(cardIndex - 1))
                            ) + scaleIn(
                                initialScale = 0.9f,
                                animationSpec = tween(durationMillis = 600, delayMillis = getCardDelay(cardIndex - 1))
                            )
                ) {
                    LuckScoreCard(result.luckScore)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Energy Score Card
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(durationMillis = 600, delayMillis = getCardDelay(cardIndex++))) +
                            slideInVertically(
                                initialOffsetY = { 30 },
                                animationSpec = tween(durationMillis = 600, delayMillis = getCardDelay(cardIndex - 1))
                            )
                ) {
                    EnergyScoreCard(result.energyScore)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Mantra Card
                if (result.mantra.isNotEmpty()) {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = tween(durationMillis = 600, delayMillis = getCardDelay(cardIndex++))) +
                                slideInVertically(
                                    initialOffsetY = { 30 },
                                    animationSpec = tween(durationMillis = 600, delayMillis = getCardDelay(cardIndex - 1))
                                )
                    ) {
                        MantraCard(result.mantra)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Main Reading Text
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(durationMillis = 600, delayMillis = getCardDelay(cardIndex++))) +
                            slideInVertically(
                                initialOffsetY = { 30 },
                                animationSpec = tween(durationMillis = 600, delayMillis = getCardDelay(cardIndex - 1))
                            )
                ) {
                    ReadingCard(
                        title = "Priča iz šalice",
                        content = result.mainText
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Categories with animations
                if (result.love?.isNotEmpty() == true) {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = tween(durationMillis = 600, delayMillis = getCardDelay(cardIndex++))) +
                                slideInVertically(
                                    initialOffsetY = { 30 },
                                    animationSpec = tween(durationMillis = 600, delayMillis = getCardDelay(cardIndex - 1))
                                )
                    ) {
                        ReadingCard(
                            title = "💕 Ljubav",
                            content = result.love
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                if (result.work?.isNotEmpty() == true) {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = tween(durationMillis = 600, delayMillis = getCardDelay(cardIndex++))) +
                                slideInVertically(
                                    initialOffsetY = { 30 },
                                    animationSpec = tween(durationMillis = 600, delayMillis = getCardDelay(cardIndex - 1))
                                )
                    ) {
                        ReadingCard(
                            title = "💼 Posao",
                            content = result.work
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                if (result.money?.isNotEmpty() == true) {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = tween(durationMillis = 600, delayMillis = getCardDelay(cardIndex++))) +
                                slideInVertically(
                                    initialOffsetY = { 30 },
                                    animationSpec = tween(durationMillis = 600, delayMillis = getCardDelay(cardIndex - 1))
                                )
                    ) {
                        ReadingCard(
                            title = "💰 Novac",
                            content = result.money
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                if (result.health?.isNotEmpty() == true) {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = tween(durationMillis = 600, delayMillis = getCardDelay(cardIndex++))) +
                                slideInVertically(
                                    initialOffsetY = { 30 },
                                    animationSpec = tween(durationMillis = 600, delayMillis = getCardDelay(cardIndex - 1))
                                )
                    ) {
                        ReadingCard(
                            title = "🌿 Zdravlje",
                            content = result.health
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Symbols
                if (result.symbols.isNotEmpty()) {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = tween(durationMillis = 600, delayMillis = getCardDelay(cardIndex++))) +
                                slideInVertically(
                                    initialOffsetY = { 30 },
                                    animationSpec = tween(durationMillis = 600, delayMillis = getCardDelay(cardIndex - 1))
                                )
                    ) {
                        SymbolsCard(result.symbols)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Lucky Numbers
                if (result.luckyNumbers.isNotEmpty()) {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = tween(durationMillis = 600, delayMillis = getCardDelay(cardIndex++))) +
                                slideInVertically(
                                    initialOffsetY = { 30 },
                                    animationSpec = tween(durationMillis = 600, delayMillis = getCardDelay(cardIndex - 1))
                                )
                    ) {
                        LuckyNumbersCard(result.luckyNumbers)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Error message
                if (saveError != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    com.gatalinka.app.ui.components.ErrorCard(
                        message = saveError ?: ErrorMessages.SAVE_FAILED,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
                
                // Save Button
                Button(
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        val currentImageUri = imageUri.split("?")[0]
                        coroutineScope.launch {
                            isSaving = true
                            saveError = null
                            try {
                                val cupReading = com.gatalinka.app.data.ReadingMapper.mapToCupReading(
                                    result,
                                    currentImageUri
                                ).copy(
                                    targetName = targetName,
                                    forSelf = targetName == null
                                )
                                readingsRepo.addReading(cupReading)
                                onSave()
                            } catch (e: Exception) {
                                saveError = when {
                                    e.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true ||
                                    e.message?.contains("permission", ignoreCase = true) == true ->
                                        ErrorMessages.PERMISSION_DENIED
                                    e.message?.contains("network", ignoreCase = true) == true ->
                                        ErrorMessages.NETWORK_ERROR
                                    else ->
                                        ErrorMessages.SAVE_FAILED
                                }
                                if (com.gatalinka.app.BuildConfig.DEBUG) {
                                    android.util.Log.e("ReadingResultScreen", "Error saving reading", e)
                                }
                            } finally {
                                isSaving = false
                            }
                        }
                    },
                    enabled = !isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GataUI.MysticGold,
                        contentColor = GataUI.MysticPurpleDeep,
                        disabledContainerColor = GataUI.MysticGold.copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = GataUI.MysticPurpleDeep,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            "Spremi čitanje",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            }
        }
    }
}

@Composable
fun GlassmorphismCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(com.gatalinka.app.ui.design.GataUI.CardCornerRadius)),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2D1B4E).copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(com.gatalinka.app.ui.design.GataUI.CardCornerRadius)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFFD700).copy(alpha = 0.1f),
                            Color(0xFF2D1B4E).copy(alpha = 0.2f)
                        )
                    )
                )
        ) {
            content()
        }
    }
}

@Composable
fun LuckScoreCard(score: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    GlassmorphismCard {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Sreća",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A0B2E)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "$score/100",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color(0xFF1A0B2E)
            )
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { score / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = Color(0xFF1A0B2E),
                trackColor = Color(0xFF1A0B2E).copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
fun ReadingCard(title: String, content: String) {
    GlassmorphismCard {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFD700),
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Text(
                content,
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFFEFE3D1),
                lineHeight = 24.sp,
                textAlign = TextAlign.Justify
            )
        }
    }
}

@Composable
fun SymbolsCard(symbols: List<String>) {
    GlassmorphismCard {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                "🔮 Prepoznati simboli",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFD700),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            // Chips layout
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                symbols.forEach { symbol ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFFFD700).copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = Color(0xFFFFD700).copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "✦",
                                color = Color(0xFFFFD700),
                                modifier = Modifier.padding(end = 6.dp)
                            )
                            Text(
                                symbol,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFFFD700),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LuckyNumbersCard(numbers: List<Int>) {
    GlassmorphismCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "🎲 Sretni brojevi",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFD700),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                numbers.forEach { number ->
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFFFD700),
                        modifier = Modifier
                            .size(60.dp)
                            .shadow(8.dp, CircleShape),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 2.dp,
                            color = Color(0xFF1A0B2E).copy(alpha = 0.3f)
                        )
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                number.toString(),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A0B2E)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EnergyScoreCard(score: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "energy_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    GlassmorphismCard {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Energija dana",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFD700)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "$score/100",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color(0xFFFFD700)
            )
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { score / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = Color(0xFFFFD700),
                trackColor = Color(0xFFFFD700).copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
fun MantraCard(mantra: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "mantra_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    GlassmorphismCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "✨ Dnevna mantra",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFD700),
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Text(
                mantra,
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFFEFE3D1),
                lineHeight = 24.sp,
                textAlign = TextAlign.Center,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }
    }
}
