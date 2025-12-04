package com.gatalinka.app.ui.screens

import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gatalinka.app.R
import com.gatalinka.app.api.FirebaseFunctionsService
import com.gatalinka.app.api.dto.GatalinkaReadingDto
import com.gatalinka.app.data.UserPreferencesRepository
import com.gatalinka.app.ui.components.ReadingGlowEffect
import com.gatalinka.app.ui.components.MysticOrb
import com.gatalinka.app.ui.design.BeanCTA
import com.gatalinka.app.ui.design.GataUI
import com.gatalinka.app.ui.model.GatalinkaReadingUiModel
import com.gatalinka.app.vm.ReadingForOthersViewModel
import com.gatalinka.app.util.ErrorMessages
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.compose.runtime.snapshotFlow

@Composable
fun ReadCupScreen(
    imageUri: String,
    readingMode: String = "instant",
    preferencesRepo: UserPreferencesRepository,
    onBack: () -> Unit,
    onViewResult: (GatalinkaReadingUiModel) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Provjeri da li postoji custom UserInput za gatanje drugim ljudima
    // Koristi isti ViewModel instance kroz navigaciju koristeći shared key
    val readingForOthersVm: ReadingForOthersViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        key = "reading_for_others_shared"
    )
    
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var result by remember { mutableStateOf<GatalinkaReadingUiModel?>(null) }
    
    // Pokreni čitanje jednom - osvježi kada se promijeni imageUri
    // Koristi viewModelScope da se API poziv ne prekine ako kompozicija napusti
    LaunchedEffect(imageUri) {
        isLoading = true
        errorMessage = null
        result = null
        
        try {
            val cleanImageUri = imageUri.split("?")[0]
            val uri = Uri.parse(cleanImageUri)
            
            // VAŽNO: Koristi customUserInput ako je potpuno postavljen (ima zodiacSign i gender)
            // Čitaj direktno iz ViewModela unutar LaunchedEffect da osiguramo najnoviju vrijednost
            // Čekaj malo da osiguramo da je ViewModel state postavljen (ako je došao iz ReadingForOthers)
            var attempts = 0
            var currentCustomUserInput: com.gatalinka.app.data.UserInput? = null
            
            // Pokušaj dohvatiti customUserInput - čekaj do 500ms ako nije postavljen
            while (attempts < 10 && currentCustomUserInput == null) {
                currentCustomUserInput = readingForOthersVm.customUserInput
                if (currentCustomUserInput == null || 
                    currentCustomUserInput.zodiacSign == null || 
                    currentCustomUserInput.gender == com.gatalinka.app.data.Gender.Unspecified) {
                    kotlinx.coroutines.delay(50)
                    attempts++
                    currentCustomUserInput = null // Reset da provjerimo ponovno
                } else {
                    break // Našli smo validan customUserInput
                }
            }
            
            // Finalna provjera
            val finalCustomUserInput = readingForOthersVm.customUserInput
            
            if (com.gatalinka.app.BuildConfig.DEBUG) {
                android.util.Log.d("ReadCupScreen", "=== LaunchedEffect Triggered ===")
                android.util.Log.d("ReadCupScreen", "imageUri: $imageUri")
                android.util.Log.d("ReadCupScreen", "Attempts: $attempts")
                android.util.Log.d("ReadCupScreen", "finalCustomUserInput: ${finalCustomUserInput != null}")
                finalCustomUserInput?.let { cui ->
                    android.util.Log.d("ReadCupScreen", "  zodiacSign: ${cui.zodiacSign?.displayName}")
                    android.util.Log.d("ReadCupScreen", "  gender: ${cui.gender.name}")
                    android.util.Log.d("ReadCupScreen", "  birthdate: ${cui.birthdate}")
                }
            }
            val userInput = if (finalCustomUserInput != null && 
                               finalCustomUserInput.zodiacSign != null && 
                               finalCustomUserInput.gender != com.gatalinka.app.data.Gender.Unspecified &&
                               finalCustomUserInput.birthdate.isNotEmpty()) {
                if (com.gatalinka.app.BuildConfig.DEBUG) {
                    android.util.Log.d("ReadCupScreen", "✅ Using CUSTOM UserInput for reading for others")
                    android.util.Log.d("ReadCupScreen", "Custom zodiac: ${finalCustomUserInput.zodiacSign?.displayName}")
                    android.util.Log.d("ReadCupScreen", "Custom gender: ${finalCustomUserInput.gender.name}")
                    android.util.Log.d("ReadCupScreen", "Custom birthdate: ${finalCustomUserInput.birthdate}")
                }
                finalCustomUserInput
            } else {
                val defaultInput = preferencesRepo.userInput.first()
                if (com.gatalinka.app.BuildConfig.DEBUG) {
                    android.util.Log.d("ReadCupScreen", "⚠️ Using DEFAULT UserInput (custom not set or incomplete)")
                    android.util.Log.d("ReadCupScreen", "Default zodiac: ${defaultInput.zodiacSign?.displayName}")
                    android.util.Log.d("ReadCupScreen", "Custom input check: null=${finalCustomUserInput == null}, " +
                        "zodiac=${finalCustomUserInput?.zodiacSign?.displayName}, " +
                        "gender=${finalCustomUserInput?.gender?.name}, " +
                        "birthdate=${finalCustomUserInput?.birthdate}")
                }
                defaultInput
            }
            
            // Debug logging samo u debug build-u
            if (com.gatalinka.app.BuildConfig.DEBUG) {
                android.util.Log.d("ReadCupScreen", "=== DEBUG: Final UserInput ===")
                android.util.Log.d("ReadCupScreen", "Using userInput: zodiac=${userInput.zodiacSign?.displayName}, gender=${userInput.gender.name}, birthdate=${userInput.birthdate}")
                android.util.Log.d("ReadCupScreen", "=== END DEBUG ===")
            }
            
            // Koristi viewModelScope da se API poziv ne prekine ako kompozicija napusti
            readingForOthersVm.readCup(
                imageUri = uri,
                context = context,
                userInput = userInput,
                readingMode = readingMode,
                onSuccess = { uiModel ->
                    result = uiModel
                    isLoading = false
                    onViewResult(uiModel)
                },
                onError = { error ->
                    errorMessage = error
                    isLoading = false
                    result = null
                }
            )
            
        } catch (e: Exception) {
            if (com.gatalinka.app.BuildConfig.DEBUG) {
                android.util.Log.e("ReadCupScreen", "Error reading cup", e)
            }
            errorMessage = ErrorMessages.getErrorMessage(e)
            isLoading = false
            result = null
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1510))
    ) {
        // Pozadina
        Image(
            painter = painterResource(id = R.drawable.coffee_cup_bg_ext),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alignment = Alignment.TopCenter,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationY = (-100).dp.toPx()
                    scaleX = 1.10f
                    scaleY = 1.10f
                }
        )

        // Scrim
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(280.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0x33000000),
                            Color(0x55000000)
                        )
                    )
                )
        )
        
        // Mystic effects dok se čita - WOW verzija
        if (isLoading) {
            // Enhanced ReadingGlowEffect
            ReadingGlowEffect(
                modifier = Modifier.fillMaxSize(),
                centerX = null,
                centerY = null,
                color = GataUI.MysticGold.copy(alpha = 0.5f)
            )
            
            // Multiple floating orbs
            MysticOrb(
                modifier = Modifier.fillMaxSize(),
                color = GataUI.MysticGold.copy(alpha = 0.3f),
                size = 100f
            )
            
            // Particles overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.6f)
            ) {
                com.gatalinka.app.ui.components.Sparkles(particleCount = 60)
            }
            
            // Mist overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                GataUI.MysticPurpleMedium.copy(alpha = 0.3f),
                                GataUI.MysticPurpleDeep.copy(alpha = 0.5f)
                            )
                        )
                    )
            )
        }

        // Sadržaj
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = GataUI.ScreenPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            AnimatedVisibility(
                visible = isLoading,
                enter = fadeIn() + slideInVertically(initialOffsetY = { 20 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { -20 })
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Animated text
                    val infiniteTransition = rememberInfiniteTransition(label = "loading_pulse")
                    val textAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.6f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "text_alpha"
                    )
                    
                    Text(
                        text = "U tijeku je ritual čitanja…",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = Color(0xFFFFE9C6),
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .alpha(textAlpha)
                    )
                    
                    Text(
                        text = "Tvoja šalica otkriva sudbinu",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color(0xFFFFE9C6).copy(alpha = 0.8f),
                            fontSize = 16.sp
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 18.dp)
                            .alpha(textAlpha * 0.8f)
                    )
                    
                    CircularProgressIndicator(
                        modifier = Modifier.padding(16.dp),
                        color = Color(0xFFFFE9C6)
                    )
                }
            }
            
            AnimatedVisibility(
                visible = errorMessage != null,
                enter = fadeIn() + slideInVertically(initialOffsetY = { 20 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { -20 })
            ) {
                if (errorMessage != null) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                    ) {
                        Text(
                            text = errorMessage ?: ErrorMessages.UNKNOWN_ERROR,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                color = Color(0xFFFFE9C6),
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 24.dp)
                        )
                        
                        // Dva gumba jedan pored drugog
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    isLoading = true
                                    errorMessage = null
                                    result = null
                                    
                                    val cleanImageUri = imageUri.split("?")[0]
                                    val uri = Uri.parse(cleanImageUri)
                                    
                                    // VAŽNO: Koristi customUserInput ako je potpuno postavljen
                                    val currentCustomUserInput = readingForOthersVm.customUserInput
                                    scope.launch {
                                        val userInput = if (currentCustomUserInput != null && 
                                                           currentCustomUserInput.zodiacSign != null && 
                                                           currentCustomUserInput.gender != com.gatalinka.app.data.Gender.Unspecified &&
                                                           currentCustomUserInput.birthdate.isNotEmpty()) {
                                            currentCustomUserInput
                                        } else {
                                            preferencesRepo.userInput.first()
                                        }
                                        
                                        readingForOthersVm.readCup(
                                            imageUri = uri,
                                            context = context,
                                            userInput = userInput,
                                            readingMode = readingMode,
                                            onSuccess = { uiModel ->
                                                result = uiModel
                                                isLoading = false
                                                onViewResult(uiModel)
                                            },
                                            onError = { error ->
                                                errorMessage = error
                                                isLoading = false
                                                result = null
                                            }
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GataUI.MysticGold,
                                    contentColor = GataUI.MysticPurpleDeep
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    "Pokušaj ponovo",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            OutlinedButton(
                                onClick = onBack,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFFFFE9C6)
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = 2.dp,
                                    color = Color(0xFFFFE9C6)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    "Nazad",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
            
            AnimatedVisibility(
                visible = result != null,
                enter = fadeIn() + slideInVertically(initialOffsetY = { 20 }),
                exit = fadeOut()
            ) {
                if (result != null) {
                    // Ovo se ne bi trebalo prikazati jer navigacija treba biti trenutna
                    CircularProgressIndicator(
                        modifier = Modifier.padding(16.dp),
                        color = Color(0xFFFFE9C6)
                    )
                    Text(
                        text = "Čitanje je gotovo!",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = Color(0xFFFFE9C6),
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 18.dp)
                    )
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}
