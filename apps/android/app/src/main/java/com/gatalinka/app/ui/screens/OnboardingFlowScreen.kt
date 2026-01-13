package com.gatalinka.app.ui.screens

import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gatalinka.app.data.Gender
import com.gatalinka.app.data.UserPreferencesRepository
import com.gatalinka.app.ui.components.MysticBackground
import com.gatalinka.app.ui.components.PulsingText
import com.gatalinka.app.ui.design.GataUI
import com.gatalinka.app.util.DateValidators
import com.gatalinka.app.util.DobFormatter
import com.gatalinka.app.util.ZodiacSign
import com.gatalinka.app.vm.OnboardingViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first

enum class OnboardingStep {
    HYPE, // Welcome ekran - "Dobrodošao u Gatalinku"
    HOW_TO_PHOTO,
    WHAT_APP_DOES,
    YOUR_DATA,
    FORM
}

@SuppressLint("UnrememberedMutableState")
@Composable
fun OnboardingFlowScreen(
    preferencesRepo: UserPreferencesRepository,
    vm: OnboardingViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onComplete: () -> Unit
) {
    var currentStep by remember { mutableStateOf(OnboardingStep.HYPE) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    
    // Učitaj postojeće podatke iz DataStore
    val savedUserInput by preferencesRepo.userInput.collectAsState(initial = com.gatalinka.app.data.UserInput())
    
    var birth by remember {
        mutableStateOf(TextFieldValue(savedUserInput.birthdate))
    }
    var gender by remember { mutableStateOf(savedUserInput.gender) }
    
    // Ažuriraj kada se učitaju podaci - SAMO ako korisnik još nije unio podatke
    LaunchedEffect(savedUserInput.birthdate) {
        // Ažuriraj samo ako je savedUserInput.birthdate različit od trenutnog i ako je birth prazan
        // Ovo sprječava resetiranje podataka koje je korisnik već unio
        if (savedUserInput.birthdate.isNotEmpty() && birth.text.isEmpty() && birth.text != savedUserInput.birthdate) {
            android.util.Log.e("GATALINKA_ONBOARDING", "LaunchedEffect: učitavam savedUserInput.birthdate='${savedUserInput.birthdate}'")
            birth = TextFieldValue(savedUserInput.birthdate)
            gender = savedUserInput.gender
        }
    }

    // Koristi derivedStateOf za brže ažuriranje kada se birth.text promijeni
    val isDateValid = androidx.compose.runtime.derivedStateOf { 
        DateValidators.isValidDob(birth.text)
    }.value
    val zodiac = androidx.compose.runtime.derivedStateOf { 
        if (isDateValid) com.gatalinka.app.util.ZodiacCalculator.calculateZodiac(birth.text) else null 
    }.value
    val isReady = androidx.compose.runtime.derivedStateOf {
        isDateValid && gender != Gender.Unspecified
    }.value

    MysticBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = GataUI.ScreenPadding)
        ) {
            // Progress indicator
            LinearProgressIndicator(
                progress = { (currentStep.ordinal.toFloat() / (OnboardingStep.entries.size - 1)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            // Content with animation
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    slideInHorizontally(
                        initialOffsetX = { fullWidth -> fullWidth },
                        animationSpec = tween(300)
                    ) + fadeIn() togetherWith
                    slideOutHorizontally(
                        targetOffsetX = { fullWidth -> -fullWidth },
                        animationSpec = tween(300)
                    ) + fadeOut()
                },
                label = "onboarding_step"
            ) { step ->
                when (step) {
                    OnboardingStep.HYPE -> HypeStep(
                        onNext = { currentStep = OnboardingStep.HOW_TO_PHOTO }
                    )
                    OnboardingStep.HOW_TO_PHOTO -> HowToPhotoStep(
                        onNext = { currentStep = OnboardingStep.WHAT_APP_DOES },
                        onBack = { currentStep = OnboardingStep.HYPE }
                    )
                    OnboardingStep.WHAT_APP_DOES -> WhatAppDoesStep(
                        onNext = { currentStep = OnboardingStep.YOUR_DATA },
                        onBack = { currentStep = OnboardingStep.HOW_TO_PHOTO }
                    )
                    OnboardingStep.YOUR_DATA -> YourDataStep(
                        onNext = { currentStep = OnboardingStep.FORM },
                        onBack = { currentStep = OnboardingStep.WHAT_APP_DOES }
                    )
                    OnboardingStep.FORM -> FormStep(
                        birth = birth,
                        onBirthChange = { tf ->
                            val (txt, pos) = DobFormatter.formatKeepingCursor(
                                tf.text,
                                tf.selection.end
                            )
                            android.util.Log.e("GATALINKA_ONBOARDING", "onBirthChange: old='${birth.text}', new='$txt'")
                            birth = TextFieldValue(txt, TextRange(pos))
                            vm.updateBirthdate(txt)
                            android.util.Log.e("GATALINKA_ONBOARDING", "onBirthChange: birth.text nakon update='${birth.text}'")
                        },
                        gender = gender,
                        onGenderChange = { g ->
                            keyboardController?.hide()
                            android.util.Log.e("GATALINKA_ONBOARDING", "onGenderChange: old=${gender.name}, new=${g.name}")
                            gender = g
                            vm.updateGender(g)
                            android.util.Log.e("GATALINKA_ONBOARDING", "onGenderChange: gender nakon update=${gender.name}")
                        },
                        scope = scope,
                        preferencesRepo = preferencesRepo,
                        vm = vm,
                        onComplete = {
                            keyboardController?.hide()
                            // onComplete se poziva direktno iz FormStep nakon što su podaci spremljeni
                            // Ovdje samo pozivamo parent onComplete za navigaciju
                            onComplete()
                        },
                        onBack = { currentStep = OnboardingStep.YOUR_DATA }
                    )
                }
            }
        }
    }
}

@Composable
private fun HypeStep(onNext: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "hype")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "icon_scale"
    )
    
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer_alpha"
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(150.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "🔮",
                fontSize = 120.sp,
                modifier = Modifier.scale(scale)
            )
            
            // Glow effect
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(shimmerAlpha)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFFD700).copy(alpha = 0.6f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }
        
        PulsingText(
            text = "Dobrodošao u Gatalinku",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 32.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )
        
        Text(
            text = "Dom mističnih čitanja",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 48.dp)
        )
        
        // Mistični circular button s pulsating glow
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val infiniteTransition3 = rememberInfiniteTransition(label = "hype_button_glow")
            val glowAlpha3 by infiniteTransition3.animateFloat(
                initialValue = 0.4f,
                targetValue = 0.8f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "glow"
            )
            
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                GataUI.MysticGold.copy(alpha = glowAlpha3),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
                    .clickable(onClick = onNext)
                    .border(
                        width = 2.dp,
                        color = GataUI.MysticGold.copy(alpha = 0.6f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🔮",
                    fontSize = 40.sp
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Započni putovanje",
                style = MaterialTheme.typography.titleMedium,
                color = GataUI.MysticGold
            )
        }
    }
}

@Composable
private fun HowToPhotoStep(onNext: () -> Unit, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.CameraAlt,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Kako slikati šalicu",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                InstructionItem("1", "Popij kavu, ali ostavi talog na dnu")
                Spacer(modifier = Modifier.height(12.dp))
                InstructionItem("2", "Okreni šalicu naglavačke i čekaj 2-3 minute")
                Spacer(modifier = Modifier.height(12.dp))
                InstructionItem("3", "Fotkaj šalicu odozgo, direktno")
                Spacer(modifier = Modifier.height(12.dp))
                InstructionItem("4", "Dobra svjetlost je ključna")
                Spacer(modifier = Modifier.height(12.dp))
                InstructionItem("5", "AI će analizirati simbole u talogu")
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Nazad gumb - ikona u krugu
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(48.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = Color(0xFF2D1B4E).copy(alpha = 0.6f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Nazad",
                        tint = GataUI.MysticGold.copy(alpha = 0.8f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            // Glavni gumb - circular s glow
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val infiniteTransition5 = rememberInfiniteTransition(label = "howto_button_glow")
                val glowAlpha5 by infiniteTransition5.animateFloat(
                    initialValue = 0.4f,
                    targetValue = 0.8f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "glow"
                )
                
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    GataUI.MysticGold.copy(alpha = glowAlpha5),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                        .clickable(onClick = onNext)
                        .border(
                            width = 2.dp,
                            color = GataUI.MysticGold.copy(alpha = 0.6f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Dalje",
                        tint = GataUI.MysticGold,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun WhatAppDoesStep(onNext: () -> Unit, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.AutoAwesome,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Što app radi?",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                FeatureItem(Icons.Default.ImageSearch, "AI analizira simbole u talogu")
                Spacer(modifier = Modifier.height(16.dp))
                FeatureItem(Icons.Default.Favorite, "Otkrij svoju sudbinu u ljubavi, poslu, novcu")
                Spacer(modifier = Modifier.height(16.dp))
                FeatureItem(Icons.Default.Star, "Dobij sretne brojeve i dnevnu mantru")
                Spacer(modifier = Modifier.height(16.dp))
                FeatureItem(Icons.Default.History, "Spremi sva čitanja i prati svoj put")
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Nazad gumb - ikona u krugu
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(48.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = Color(0xFF2D1B4E).copy(alpha = 0.6f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Nazad",
                        tint = GataUI.MysticGold.copy(alpha = 0.8f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            // Glavni gumb - circular s glow
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val infiniteTransition6 = rememberInfiniteTransition(label = "whatapp_button_glow")
                val glowAlpha6 by infiniteTransition6.animateFloat(
                    initialValue = 0.4f,
                    targetValue = 0.8f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "glow"
                )
                
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    GataUI.MysticGold.copy(alpha = glowAlpha6),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                        .clickable(onClick = onNext)
                        .border(
                            width = 2.dp,
                            color = GataUI.MysticGold.copy(alpha = 0.6f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Dalje",
                        tint = GataUI.MysticGold,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun YourDataStep(onNext: () -> Unit, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Info,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Koje podatke koristimo?",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Za personalizirano čitanje trebamo:",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
                
                DataItem("📅", "Datum rođenja", "Za izračun horoskopskog znaka")
                Spacer(modifier = Modifier.height(12.dp))
                DataItem("👤", "Spol", "Za prilagođeno tumačenje")
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    "Svi podaci su sigurni i privatni. Koristimo ih samo za tvoje čitanje!",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Nazad gumb - ikona u krugu
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(48.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = Color(0xFF2D1B4E).copy(alpha = 0.6f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Nazad",
                        tint = GataUI.MysticGold.copy(alpha = 0.8f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            // Glavni gumb - circular s glow
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val infiniteTransition7 = rememberInfiniteTransition(label = "yourdata_button_glow")
                val glowAlpha7 by infiniteTransition7.animateFloat(
                    initialValue = 0.4f,
                    targetValue = 0.8f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "glow"
                )
                
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    GataUI.MysticGold.copy(alpha = glowAlpha7),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                        .clickable(onClick = onNext)
                        .border(
                            width = 2.dp,
                            color = GataUI.MysticGold.copy(alpha = 0.6f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Dalje",
                        tint = GataUI.MysticGold,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FormStep(
    birth: TextFieldValue,
    onBirthChange: (TextFieldValue) -> Unit,
    gender: Gender,
    onGenderChange: (Gender) -> Unit,
    scope: CoroutineScope,
    preferencesRepo: UserPreferencesRepository,
    vm: OnboardingViewModel,
    onComplete: () -> Unit,
    onBack: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    
    // Loguj vrijednosti kada se FormStep recomponira
    LaunchedEffect(birth.text, gender) {
        android.util.Log.e("GATALINKA_ONBOARDING", "FormStep recomponiran: birth.text='${birth.text}', gender=${gender.name}")
    }
    
    // Računaj validacije direktno u FormStep da se ažuriraju kada se birth ili gender promijene
    val isDateValid by remember(birth.text) { 
        derivedStateOf { 
            val valid = DateValidators.isValidDob(birth.text)
            android.util.Log.e("GATALINKA_ONBOARDING", "FormStep: isDateValid=$valid za birth.text='${birth.text}'")
            valid
        }
    }
    val zodiac by remember(birth.text, isDateValid) { 
        derivedStateOf { 
            val z = if (isDateValid) com.gatalinka.app.util.ZodiacCalculator.calculateZodiac(birth.text) else null
            android.util.Log.e("GATALINKA_ONBOARDING", "FormStep: zodiac=${z?.displayName} za birth.text='${birth.text}'")
            z
        }
    }
    val isReady by remember(isDateValid, gender) { 
        derivedStateOf { 
            val ready = isDateValid && gender != Gender.Unspecified
            android.util.Log.e("GATALINKA_ONBOARDING", "FormStep: isReady=$ready (isDateValid=$isDateValid, gender=${gender.name})")
            ready
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 24.dp)
            .pointerInput(Unit) {
                detectTapGestures {
                    keyboardController?.hide()
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Tvoji podaci",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = birth,
            onValueChange = onBirthChange,
            label = { Text("Datum rođenja") },
            placeholder = { Text("Dan kad si rođen/a") },
            isError = birth.text.isNotBlank() && birth.text.length >= 10 && !isDateValid,
            supportingText = {
                if (birth.text.isNotBlank() && birth.text.length >= 10 && !isDateValid) {
                    Text("Unesite ispravan datum, npr. 05.11.1990")
                } else if (birth.text.isNotBlank() && birth.text.length < 10) {
                    Text("Unesite datum u formatu DD.MM.GGGG")
                }
                // SAKRIVAMO prikaz znaka (Ovan) - znak ostaje interno, ali se ne prikazuje
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (isDateValid) {
                        keyboardController?.hide()
                    }
                }
            ),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        )

        GenderSelector(
            selected = gender,
            onSelect = { g ->
                keyboardController?.hide()
                onGenderChange(g)
            },
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Nazad gumb - ikona u krugu umjesto teksta
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(48.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = Color(0xFF2D1B4E).copy(alpha = 0.6f),
                            shape = androidx.compose.foundation.shape.CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Nazad",
                        tint = GataUI.MysticGold.copy(alpha = 0.8f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            // Glavni gumb - veći, magičniji, s pulsirajućim glow-om
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                // Pulsirajući glow efekt
                val infiniteTransition = rememberInfiniteTransition(label = "button_glow")
                val glowAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.4f,
                    targetValue = 0.8f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "glow"
                )
                
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    GataUI.MysticGold.copy(alpha = glowAlpha),
                                    Color.Transparent
                                )
                            ),
                            shape = androidx.compose.foundation.shape.CircleShape
                        )
                        .clickable(enabled = isReady) {
                            if (isReady) {
                                android.util.Log.e("GATALINKA_ONBOARDING", "=== GUMB KLIKNUT ===")
                                android.util.Log.e("GATALINKA_ONBOARDING", "isReady=$isReady")
                                android.util.Log.e("GATALINKA_ONBOARDING", "birth.text='${birth.text}'")
                                android.util.Log.e("GATALINKA_ONBOARDING", "gender=${gender.name}")
                                android.util.Log.e("GATALINKA_ONBOARDING", "isDateValid=$isDateValid")
                                android.util.Log.e("GATALINKA_ONBOARDING", "zodiac=${zodiac?.displayName}")
                                
                                android.util.Log.e("GATALINKA_ONBOARDING", ">>> isReady=TRUE, SPREMA PODATKE")
                                keyboardController?.hide()
                                scope.launch {
                                    try {
                                        // Spremi podatke
                                        preferencesRepo.saveUserInput(
                                            com.gatalinka.app.data.UserInput(
                                                birthdate = birth.text,
                                                gender = gender,
                                                zodiacSign = zodiac,
                                                acceptedDisclaimer = true
                                            )
                                        )
                                        vm.acceptDisclaimer()
                                        
                                        // Čekaj malo da se DataStore edit završi
                                        kotlinx.coroutines.delay(100)
                                        
                                        // Provjeri da li je onboarding zaista završen
                                        val hasCompleted = preferencesRepo.hasCompletedOnboarding.first()
                                        android.util.Log.e("GATALINKA_ONBOARDING", ">>> hasCompletedOnboarding nakon spremanja: $hasCompleted")
                                        
                                        android.util.Log.e("GATALINKA_ONBOARDING", ">>> Podaci spremljeni, pozivam onComplete")
                                        // Navigiraj na UI thread
                                        withContext(Dispatchers.Main) {
                                            onComplete()
                                        }
                                    } catch (e: Exception) {
                                        android.util.Log.e("GATALINKA_ONBOARDING", ">>> GREŠKA pri spremanju: ${e.message}", e)
                                        e.printStackTrace()
                                        // Ipak navigiraj da korisnik ne ostane zaglavljen
                                        withContext(Dispatchers.Main) {
                                            onComplete()
                                        }
                                    }
                                }
                            } else {
                                android.util.Log.e("GATALINKA_ONBOARDING", ">>> isReady=FALSE, NE SPREMA PODATKE")
                                android.util.Log.e("GATALINKA_ONBOARDING", ">>> isDateValid=$isDateValid, gender=${gender.name}")
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                color = GataUI.MysticPurpleMedium,
                                shape = androidx.compose.foundation.shape.CircleShape
                            )
                            .border(
                                width = 3.dp,
                                color = GataUI.MysticGold,
                                shape = androidx.compose.foundation.shape.CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isReady) "🔮" else "☕",
                            fontSize = 32.sp
                        )
                    }
                }
                
                // Tekst ispod gumba - bapski stil
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (isReady) "Zavirimo u šalicu" else "Popuni sve",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = GataUI.Beige.copy(alpha = if (isReady) 1f else 0.6f),
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                    ),
                    textAlign = TextAlign.Center
                )
            }
            
            // Prazan prostor za balans
            Spacer(modifier = Modifier.size(48.dp))
        }
    }
}

@Composable
private fun InstructionItem(number: String, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.primary
        ) {
            Text(
                number,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun FeatureItem(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun DataItem(emoji: String, title: String, description: String) {
    Row(
        verticalAlignment = Alignment.Top
    ) {
        Text(
            emoji,
            fontSize = 24.sp,
            modifier = Modifier.padding(end = 12.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun GenderSelector(
    selected: Gender,
    onSelect: (Gender) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            "Spol",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            GenderOption(
                label = "Muški",
                selected = selected == Gender.Male,
                onClick = { onSelect(Gender.Male) }
            )
            GenderOption(
                label = "Ženski",
                selected = selected == Gender.Female,
                onClick = { onSelect(Gender.Female) }
            )
            GenderOption(
                label = "Drugo",
                selected = selected == Gender.Other,
                onClick = { onSelect(Gender.Other) }
            )
        }
    }
}

@Composable
private fun GenderOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) }
    )
}

