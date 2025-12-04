package com.gatalinka.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gatalinka.app.api.FirebaseFunctionsService
import com.gatalinka.app.data.UserPreferencesRepository
import com.gatalinka.app.ui.model.GatalinkaReadingUiModel
import com.gatalinka.app.ui.components.MysticBackground
import com.gatalinka.app.ui.components.Sparkles
import com.gatalinka.app.ui.components.LoadingScreen
import com.gatalinka.app.ui.components.ErrorCard
import com.gatalinka.app.util.ErrorMessages
import com.gatalinka.app.vm.ReadingViewModel
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyReadingScreen(
    preferencesRepo: UserPreferencesRepository,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var result by remember { mutableStateOf<GatalinkaReadingUiModel?>(null) }
    
    // Učitaj dnevno čitanje
    LaunchedEffect(Unit) {
        isLoading = true
        error = null
        try {
            val userInput = preferencesRepo.userInput.first()
            val response = FirebaseFunctionsService.getDailyReading(userInput)
            
            val uiModel = GatalinkaReadingUiModel(
                mainText = response.mainText,
                love = response.love,
                work = response.work,
                money = response.money,
                health = response.health,
                symbols = response.symbols,
                luckyNumbers = response.luckyNumbers,
                luckScore = response.luckScore,
                mantra = response.mantra,
                energyScore = response.energyScore
            )
            
            result = uiModel
        } catch (e: Exception) {
            error = ErrorMessages.getErrorMessage(e)
        } finally {
            isLoading = false
        }
    }

    MysticBackground {
        // Content
        when {
                isLoading -> {
                    LoadingScreen(message = "Učitavam dnevno čitanje...")
                }
                error != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Top Bar za error state
                        TopAppBar(
                            title = {
                                Text(
                                    "Dnevno čitanje",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color(0xFFFFD700)
                                )
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
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.Transparent
                            )
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            ErrorCard(
                                message = error ?: ErrorMessages.UNKNOWN_ERROR,
                                onRetry = {
                                    coroutineScope.launch {
                                        isLoading = true
                                        error = null
                                        try {
                                            val userInput = preferencesRepo.userInput.first()
                                            val response = FirebaseFunctionsService.getDailyReading(userInput)
                                            
                                            val uiModel = GatalinkaReadingUiModel(
                                                mainText = response.mainText,
                                                love = response.love,
                                                work = response.work,
                                                money = response.money,
                                                health = response.health,
                                                symbols = response.symbols,
                                                luckyNumbers = response.luckyNumbers,
                                                luckScore = response.luckScore,
                                                mantra = response.mantra,
                                                energyScore = response.energyScore
                                            )
                                            
                                            result = uiModel
                                        } catch (e: Exception) {
                                            error = ErrorMessages.getErrorMessage(e)
                                        } finally {
                                            isLoading = false
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
                result != null -> {
                    // Koristi isti ReadingResultScreen za prikaz s istim stilom
                    // ReadingResultScreen već ima svoj scroll, padding i TopAppBar
                    ReadingResultScreen(
                        result = result!!,
                        imageUri = "daily_reading_placeholder", // Placeholder za dnevno čitanje (nema slike)
                        onBack = onBack,
                        onSave = { /* Daily reading se automatski sprema u backend */ }
                    )
                }
            }
    }
}

