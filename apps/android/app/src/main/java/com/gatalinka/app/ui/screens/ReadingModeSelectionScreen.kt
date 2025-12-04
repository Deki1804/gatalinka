package com.gatalinka.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gatalinka.app.ui.components.MysticBackground

enum class ReadingModeType(val displayName: String, val description: String, val icon: String) {
    INSTANT("Instant misticno", "Brzo i direktno čitanje", "⚡"),
    DEEP("Duboko čitanje", "Najdetaljnije i najpreciznije čitanje", "🔮")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingModeSelectionScreen(
    onBack: () -> Unit,
    onModeSelected: (String) -> Unit // readingMode string
) {
    var selectedMode by remember { mutableStateOf<ReadingModeType?>(null) }
    
    MysticBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Top Bar
            TopAppBar(
                title = {
                    Text(
                        "Rituali čitanja",
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
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                "Odaberi način čitanja",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFD700),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                "Različiti načini čitanja pružaju različite razine detalja i dubine",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFEFE3D1).copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Mode Selection Cards
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ReadingModeType.values().forEach { mode ->
                    Card(
                        onClick = { selectedMode = mode },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedMode == mode) 
                                Color(0xFFFFD700).copy(alpha = 0.3f)
                            else 
                                Color(0xFF2D1B4E).copy(alpha = 0.8f)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        border = if (selectedMode == mode) {
                            androidx.compose.foundation.BorderStroke(
                                width = 2.dp,
                                color = Color(0xFFFFD700)
                            )
                        } else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                mode.icon,
                                fontSize = 48.sp,
                                modifier = Modifier.padding(end = 20.dp)
                            )
                            
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    mode.displayName,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFD700)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    mode.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFFEFE3D1).copy(alpha = 0.8f)
                                )
                            }
                            
                            if (selectedMode == mode) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Odabrano",
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Continue Button
            Button(
                onClick = {
                    selectedMode?.let { mode ->
                        val modeString = when (mode) {
                            ReadingModeType.INSTANT -> "instant"
                            ReadingModeType.DEEP -> "deep"
                        }
                        onModeSelected(modeString)
                    }
                },
                enabled = selectedMode != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFD700),
                    contentColor = Color(0xFF1A0B2E),
                    disabledContainerColor = Color(0xFFFFD700).copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    "Nastavi",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

