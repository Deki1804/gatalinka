package com.gatalinka.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.tween
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.gatalinka.app.ui.design.GataUI
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.*
import com.gatalinka.app.ui.components.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyReadingsScreen(
    onBack: () -> Unit,
    onReadingClick: (String) -> Unit,
    onNewReading: () -> Unit = {}
) {
    val readingsRepo = remember { com.gatalinka.app.data.CloudReadingsRepository() }
    val allReadings by readingsRepo.getReadingsSorted().collectAsState(initial = emptyList())
    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    
    // Filter state
    var showFilter by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("Sve") }
    
    // Filter readings
    val readings = remember(allReadings, selectedFilter) {
        when (selectedFilter) {
            "Danas" -> {
                val today = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                allReadings.filter { it.timestamp >= today }
            }
            "Ovaj tjedan" -> {
                val weekAgo = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -7)
                }.timeInMillis
                allReadings.filter { it.timestamp >= weekAgo }
            }
            "Ovaj mjesec" -> {
                val monthAgo = Calendar.getInstance().apply {
                    add(Calendar.MONTH, -1)
                }.timeInMillis
                allReadings.filter { it.timestamp >= monthAgo }
            }
            else -> allReadings
        }
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
                        "Moja čitanja",
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
                    IconButton(onClick = { showFilter = !showFilter }) {
                        Icon(
                            Icons.Default.FilterList,
                            "Filter",
                            tint = if (showFilter) Color(0xFFFFD700) else Color(0xFFEFE3D1)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
            
            // Filter dropdown
            AnimatedVisibility(
                visible = showFilter,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2D1B4E).copy(alpha = 0.9f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf("Sve", "Danas", "Ovaj tjedan", "Ovaj mjesec").forEach { filter ->
                            FilterChip(
                                selected = selectedFilter == filter,
                                onClick = { selectedFilter = filter },
                                label = { Text(filter, style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFFFD700),
                                    selectedLabelColor = Color(0xFF1A0B2E),
                                    containerColor = Color.White.copy(alpha = 0.1f),
                                    labelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

        if (readings.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(GataUI.ScreenPadding),
                contentAlignment = Alignment.Center
            ) {
                com.gatalinka.app.ui.components.EmptyState(
                    emoji = "☕",
                    title = "Nema spremljenih čitanja",
                    subtitle = "Fotkaj svoju prvu šalicu\ni otkrij svoju sudbinu!",
                    actionLabel = "Napravi prvo čitanje",
                    onAction = onNewReading
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentPadding = PaddingValues(0.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = readings,
                    key = { it.id }
                ) { reading ->
                    // Animated entrance
                    val cardIndex = readings.indexOf(reading)
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = tween(400, delayMillis = cardIndex * 50)) +
                                slideInVertically(
                                    initialOffsetY = { 30 },
                                    animationSpec = tween(400, delayMillis = cardIndex * 50)
                                ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ReadingCard(
                            reading = reading,
                            dateFormat = dateFormat,
                            onClick = { onReadingClick(reading.id) }
                        )
                    }
                }
            }
        }
        }
    }
}

@Composable
private fun ReadingCard(
    reading: com.gatalinka.app.data.CupReading,
    dateFormat: SimpleDateFormat,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    // Odredi naslov ovisno o tome je li za sebe ili za drugu osobu
    val cardTitle = if (!reading.forSelf && reading.targetName != null) {
        "Čitanje za: ${reading.targetName}"
    } else {
        "Moje čitanje"
    }
    
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box {
            // Image with caching and optimization - optimizirano za grid prikaz
            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(context)
                        .data(reading.imageUri)
                        .memoryCacheKey(reading.imageUri) // Enable memory cache
                        .diskCacheKey(reading.imageUri) // Enable disk cache
                        .crossfade(true) // Smooth fade-in
                        .size(coil.size.Size(200, 200)) // Smanjeno za grid performanse
                        .build()
                ),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Color.Black.copy(alpha = 0.4f)
                    )
            )

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Title and Score
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Title
                    Text(
                        text = cardTitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                    // Score
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFFD700)
                    ) {
                        Text(
                            text = "${reading.happinessScore}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.titleSmall,
                            color = Color(0xFF1A0B2E),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Date
                Text(
                    text = dateFormat.format(Date(reading.timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFEFE3D1),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

