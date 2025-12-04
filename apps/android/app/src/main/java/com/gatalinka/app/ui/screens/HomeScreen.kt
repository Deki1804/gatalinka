package com.gatalinka.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Refresh
import com.gatalinka.app.vm.AuthViewModel
import com.gatalinka.app.ui.components.MysticBackground

@Composable
fun HomeScreen(
    onReadCup: () -> Unit,
    onMyReadings: () -> Unit = {},
    onSchool: () -> Unit = {},
    onDailyReading: () -> Unit = {},
    onLogin: () -> Unit = {},
    onProfile: () -> Unit = {},
    onReadingForOthers: () -> Unit = {},
    preferencesRepo: com.gatalinka.app.data.UserPreferencesRepository? = null
) {
    val viewModel: AuthViewModel = viewModel()
    val authState by viewModel.authState.collectAsState()
    val isAuthenticated = authState is com.gatalinka.app.vm.AuthState.Authenticated
    
    // Daily reading state
    val scope = rememberCoroutineScope()
    var dailyReading by remember { mutableStateOf<com.gatalinka.app.ui.model.GatalinkaReadingUiModel?>(null) }
    var isLoadingDaily by remember { mutableStateOf(false) }
    
    // Load daily reading if authenticated - s cache support
    LaunchedEffect(isAuthenticated, preferencesRepo) {
        if (isAuthenticated && preferencesRepo != null) {
            isLoadingDaily = true
            try {
                // Prvo provjeri cache
                val cachedReading = preferencesRepo.getDailyReadingCache()
                if (cachedReading != null) {
                    dailyReading = cachedReading
                    isLoadingDaily = false
                    // Učitaj novi u pozadini za sutra
                    scope.launch {
                        try {
                            val userInput = preferencesRepo.userInput.first()
                            val response = com.gatalinka.app.api.FirebaseFunctionsService.getDailyReading(userInput)
                            val newReading = com.gatalinka.app.ui.model.GatalinkaReadingUiModel(
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
                            dailyReading = newReading
                            preferencesRepo.saveDailyReadingCache(newReading)
                        } catch (e: Exception) {
                            // Silent fail - daily reading is optional
                            if (com.gatalinka.app.BuildConfig.DEBUG) {
                                android.util.Log.w("HomeScreen", "Failed to refresh daily reading", e)
                            }
                        }
                    }
                } else {
                    // Nema cache, učitaj novi
                    val userInput = preferencesRepo.userInput.first()
                    val response = com.gatalinka.app.api.FirebaseFunctionsService.getDailyReading(userInput)
                    val newReading = com.gatalinka.app.ui.model.GatalinkaReadingUiModel(
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
                    dailyReading = newReading
                    preferencesRepo.saveDailyReadingCache(newReading)
                }
            } catch (e: Exception) {
                // Silent fail - daily reading is optional
                if (com.gatalinka.app.BuildConfig.DEBUG) {
                    android.util.Log.w("HomeScreen", "Failed to load daily reading", e)
                }
            } finally {
                isLoadingDaily = false
            }
        }
    }

    MysticBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top section - Logo/Title
            Spacer(modifier = Modifier.height(60.dp))
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 40.dp)
            ) {
                Text(
                    text = "Gatalinka",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700) // Mystic Gold
                    ),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Otkrivaj sudbinu u šalici kafe",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color(0xFFEFE3D1).copy(alpha = 0.8f) // Mystic Text Light
                    ),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Center - Action Buttons (samo ako je prijavljen)
            if (isAuthenticated) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 20.dp)
                ) {
                    
                    // Daily Reading Card with preview
                    Card(
                        onClick = onDailyReading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF2D1B4E).copy(alpha = 0.8f)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        "✨ Dnevna poruka",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFFD700)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    when {
                                        isLoadingDaily -> {
                                            Text(
                                                "Učitavam...",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color(0xFFEFE3D1).copy(alpha = 0.6f)
                                            )
                                        }
                                        dailyReading != null -> {
                                            // Animated text fade-in
                                            val textAlpha = remember { androidx.compose.animation.core.Animatable(0f) }
                                            LaunchedEffect(dailyReading) {
                                                textAlpha.animateTo(1f, animationSpec = tween(800))
                                            }
                                            
                                            Text(
                                                dailyReading!!.mainText.take(120) + if (dailyReading!!.mainText.length > 120) "..." else "",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color(0xFFEFE3D1).copy(alpha = 0.9f),
                                                maxLines = 2,
                                                modifier = Modifier.alpha(textAlpha.value)
                                            )
                                        }
                                        else -> {
                                            Text(
                                                "Tvoja sudbina za danas",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color(0xFFEFE3D1).copy(alpha = 0.8f)
                                            )
                                        }
                                    }
                                }
                                
                                IconButton(
                                    onClick = {
                                        if (preferencesRepo != null) {
                                            scope.launch {
                                                isLoadingDaily = true
                                                try {
                                                    val userInput = preferencesRepo.userInput.first()
                                                    val response = com.gatalinka.app.api.FirebaseFunctionsService.getDailyReading(userInput)
                                                    val newReading = com.gatalinka.app.ui.model.GatalinkaReadingUiModel(
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
                                                    dailyReading = newReading
                                                    preferencesRepo.saveDailyReadingCache(newReading)
                                                } catch (e: Exception) {
                                                    if (com.gatalinka.app.BuildConfig.DEBUG) {
                                                        android.util.Log.w("HomeScreen", "Failed to reload daily reading", e)
                                                    }
                                                } finally {
                                                    isLoadingDaily = false
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Refresh,
                                        "Osvježi",
                                        tint = Color(0xFFFFD700),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Main Action Buttons
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Rituali čitanja
                        ActionButton(
                            title = "Rituali čitanja",
                            subtitle = "Fotkaj svoju šalicu",
                            icon = "☕",
                            onClick = onReadCup,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        // Gatanje za druge
                        ActionButton(
                            title = "Gatanje za druge",
                            subtitle = "Gataj za prijatelje i obitelj",
                            icon = "🔮",
                            onClick = onReadingForOthers,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        // Škola čitanja
                        ActionButton(
                            title = "Škola čitanja",
                            subtitle = "Nauči simbole i značenja",
                            icon = "📚",
                            onClick = onSchool,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        // Dnevni savjet (mantra) - prikazuje se samo ako postoji mantra
                        if (dailyReading != null && dailyReading!!.mantra.isNotEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFFFD700).copy(alpha = 0.2f)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        "💫 Dnevni savjet",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFFD700),
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    Text(
                                        dailyReading!!.mantra,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFFEFE3D1),
                                        maxLines = 3
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Ako nije prijavljen, prikaži poruku
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 40.dp)
                ) {
                    Text(
                        text = "Prijavi se da nastaviš",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color(0xFFEFE3D1).copy(alpha = 0.7f)
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bottom - Profile or Login
            if (isAuthenticated) {
                // Profile button - ako je prijavljen
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(
                        onClick = onProfile
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color(0xFFFFD700)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = when (val state = authState) {
                                    is com.gatalinka.app.vm.AuthState.Authenticated -> {
                                        state.displayName ?: state.email?.split("@")?.first() ?: "Profil"
                                    }
                                    else -> "Profil"
                                },
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color(0xFFFFD700),
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }
            } else {
                // Login button - ako nije prijavljen
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(
                        onClick = onLogin
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color(0xFFFFD700)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Prijavi se",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color(0xFFFFD700),
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }
            }
            
            // Extra padding for bottom navigation bar
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun ActionButton(
    title: String,
    subtitle: String,
    icon: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2D1B4E).copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    icon,
                    fontSize = 32.sp,
                    modifier = Modifier.padding(end = 16.dp)
                )
                Column {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFEFE3D1).copy(alpha = 0.7f)
                    )
                }
            }
            Text(
                "→",
                fontSize = 24.sp,
                color = Color(0xFFFFD700),
                fontWeight = FontWeight.Bold
            )
        }
    }
}