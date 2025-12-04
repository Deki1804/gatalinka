package com.gatalinka.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavHostController
import com.gatalinka.app.nav.Routes
import com.gatalinka.app.vm.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onSignOut: () -> Unit,
    navController: NavHostController,
    viewModel: AuthViewModel = viewModel()
) {
    val authState by viewModel.authState.collectAsState()
    var showSignOutDialog by remember { mutableStateOf(false) }

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
                        "Postavke",
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

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Account Section
                Text(
                    "Račun",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2D1B4E).copy(alpha = 0.8f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column {
                        // Email/Account Info
                        when (val state = authState) {
                            is com.gatalinka.app.vm.AuthState.Authenticated -> {
                                ListItem(
                                    headlineContent = {
                                        Text(
                                            state.email ?: "Korisnik",
                                            color = Color(0xFFEFE3D1),
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    },
                                    supportingContent = {
                                        Text(
                                            "Prijavljen kao",
                                            color = Color(0xFFEFE3D1).copy(alpha = 0.7f),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    },
                                    leadingContent = {
                                        Icon(
                                            Icons.Default.Person,
                                            contentDescription = null,
                                            tint = Color(0xFFFFD700)
                                        )
                                    }
                                )
                            }
                            else -> {
                                ListItem(
                                    headlineContent = {
                                        Text(
                                            "Nisi prijavljen",
                                            color = Color(0xFFEFE3D1),
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                )
                            }
                        }

                        HorizontalDivider(color = Color(0xFFEFE3D1).copy(alpha = 0.2f))

                        // Sign Out Button
                        if (authState is com.gatalinka.app.vm.AuthState.Authenticated) {
                            ListItem(
                                headlineContent = {
                                    Text(
                                        "Odjavi se",
                                        color = Color(0xFFFF5252),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                },
                                leadingContent = {
                                    Icon(
                                        Icons.Default.ExitToApp,
                                        contentDescription = null,
                                        tint = Color(0xFFFF5252)
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { showSignOutDialog = true }
                            )
                        }
                    }
                }

                // App Settings Section
                Text(
                    "Aplikacija",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2D1B4E).copy(alpha = 0.8f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column {
                        // Notifications (placeholder)
                        ListItem(
                            headlineContent = {
                                Text(
                                    "Obavijesti",
                                    color = Color(0xFFEFE3D1),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            supportingContent = {
                                Text(
                                    "Uključi obavijesti za nova čitanja",
                                    color = Color(0xFFEFE3D1).copy(alpha = 0.7f),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            },
                            leadingContent = {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700)
                                )
                            },
                            trailingContent = {
                                Switch(
                                    checked = false, // TODO: Implement
                                    onCheckedChange = { }
                                )
                            }
                        )

                        HorizontalDivider(color = Color(0xFFEFE3D1).copy(alpha = 0.2f))

                        // Language (placeholder)
                        ListItem(
                            headlineContent = {
                                Text(
                                    "Jezik",
                                    color = Color(0xFFEFE3D1),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            supportingContent = {
                                Text(
                                    "Hrvatski",
                                    color = Color(0xFFEFE3D1).copy(alpha = 0.7f),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            },
                            leadingContent = {
                                Icon(
                                    Icons.Default.Language,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700)
                                )
                            }
                        )
                    }
                }

                // About Section
                Text(
                    "O aplikaciji",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2D1B4E).copy(alpha = 0.8f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column {
                        // Version
                        ListItem(
                            headlineContent = {
                                Text(
                                    "Verzija",
                                    color = Color(0xFFEFE3D1),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            supportingContent = {
                                Text(
                                    "1.0.0",
                                    color = Color(0xFFEFE3D1).copy(alpha = 0.7f),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            },
                            leadingContent = {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700)
                                )
                            }
                        )

                        HorizontalDivider(color = Color(0xFFEFE3D1).copy(alpha = 0.2f))

                        // Privacy Policy
                        ListItem(
                            headlineContent = {
                                Text(
                                    "Politika privatnosti",
                                    color = Color(0xFFEFE3D1),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            supportingContent = {
                                Text(
                                    "Kako čuvamo tvoje podatke",
                                    color = Color(0xFFEFE3D1).copy(alpha = 0.7f),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            },
                            leadingContent = {
                                Icon(
                                    Icons.Default.PrivacyTip,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { navController.navigate(Routes.PrivacyPolicy) }
                        )

                        HorizontalDivider(color = Color(0xFFEFE3D1).copy(alpha = 0.2f))

                        // Terms of Use
                        ListItem(
                            headlineContent = {
                                Text(
                                    "Uvjeti korištenja",
                                    color = Color(0xFFEFE3D1),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            supportingContent = {
                                Text(
                                    "Pravila korištenja aplikacije",
                                    color = Color(0xFFEFE3D1).copy(alpha = 0.7f),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            },
                            leadingContent = {
                                Icon(
                                    Icons.Default.Gavel,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { navController.navigate(Routes.TermsOfUse) }
                        )
                    }
                }
            }
        }
    }

    // Sign Out Dialog
    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = {
                Text(
                    "Odjavi se?",
                    color = Color(0xFFFFD700)
                )
            },
            text = {
                Text(
                    "Želiš li se zaista odjaviti?",
                    color = Color(0xFFEFE3D1)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.signOut()
                        showSignOutDialog = false
                        onSignOut()
                    }
                ) {
                    Text("Odjavi se", color = Color(0xFFFF5252))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSignOutDialog = false }
                ) {
                    Text("Odustani", color = Color(0xFFEFE3D1))
                }
            },
            containerColor = Color(0xFF2D1B4E),
            shape = RoundedCornerShape(16.dp)
        )
    }
}

