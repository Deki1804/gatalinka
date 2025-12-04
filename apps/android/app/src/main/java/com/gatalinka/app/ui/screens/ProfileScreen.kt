package com.gatalinka.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import com.gatalinka.app.vm.AuthViewModel
import com.gatalinka.app.data.ReadingMapper
import com.gatalinka.app.ui.components.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onSettings: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val authState by viewModel.authState.collectAsState()
    val readingsRepo = remember { com.gatalinka.app.data.CloudReadingsRepository() }
    val readings by readingsRepo.getReadingsSorted().collectAsState(initial = emptyList())
    
    // Izračunaj statistike
    val stats = remember(readings) {
        val totalReadings = readings.size
        val avgLuckScore = if (readings.isNotEmpty()) {
            readings.mapNotNull { reading ->
                val uiModel = ReadingMapper.mapToUiModel(reading)
                uiModel.luckScore.takeIf { it > 0 }
            }.average().toInt()
        } else 0
        val avgEnergyScore = if (readings.isNotEmpty()) {
            readings.mapNotNull { reading ->
                reading.energyScore.takeIf { it > 0 }
            }.average().toInt()
        } else 0
        
        Triple(totalReadings, avgLuckScore, avgEnergyScore)
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A0B2E), // Mystic Purple Deep
                        Color(0xFF2D1B4E)  // Mystic Purple Medium
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar
            TopAppBar(
                title = {
                    Text(
                        "Moj profil",
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
                actions = {
                    IconButton(onClick = onSettings) {
                        Icon(
                            Icons.Default.Settings,
                            "Postavke",
                            tint = Color(0xFFEFE3D1)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                // Profile Avatar
                Surface(
                    modifier = Modifier.size(120.dp),
                    shape = CircleShape,
                    color = Color(0xFFFFD700)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = authState.let { 
                                if (it is com.gatalinka.app.vm.AuthState.Authenticated) {
                                    it.displayName?.take(1)?.uppercase() ?: it.email?.take(1)?.uppercase() ?: "U"
                                } else {
                                    "?"
                                }
                            },
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFF1A0B2E)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // User Info
                when (val state = authState) {
                    is com.gatalinka.app.vm.AuthState.Authenticated -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF2D1B4E).copy(alpha = 0.8f)
                            ),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp)
                            ) {
                                // Display Name
                                if (state.displayName != null) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Person,
                                            contentDescription = null,
                                            tint = Color(0xFFFFD700),
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                "Ime",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color(0xFFEFE3D1).copy(alpha = 0.7f)
                                            )
                                            Text(
                                                state.displayName,
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = Color(0xFFEFE3D1),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                // Email
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Email,
                                        contentDescription = null,
                                        tint = Color(0xFFFFD700),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            "Email",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFFEFE3D1).copy(alpha = 0.7f)
                                        )
                                        Text(
                                            state.email ?: "N/A",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = Color(0xFFEFE3D1),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Statistike
                        if (stats.first > 0) {
                            Text(
                                "Statistike",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color(0xFFFFD700),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                StatCard(
                                    icon = Icons.Default.History,
                                    label = "Čitanja",
                                    value = "${stats.first}",
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                StatCard(
                                    icon = Icons.Default.Star,
                                    label = "Prosječna sreća",
                                    value = "${stats.second}",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            
                            if (stats.third > 0) {
                                Spacer(modifier = Modifier.height(12.dp))
                                StatCard(
                                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                                    label = "Prosječna energija",
                                    value = "${stats.third}",
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        } else {
                            com.gatalinka.app.ui.components.EmptyState(
                                emoji = "📖",
                                title = "Još nemaš čitanja",
                                subtitle = "Fotkaj svoju prvu šalicu!",
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    else -> {
                        Text(
                            "Nisi prijavljen",
                            color = Color(0xFFEFE3D1),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2D1B4E).copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Color(0xFFFFD700),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                value,
                style = MaterialTheme.typography.headlineSmall,
                color = Color(0xFFFFD700),
                fontWeight = FontWeight.Bold
            )
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFEFE3D1).copy(alpha = 0.7f)
            )
        }
    }
}

