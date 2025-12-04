package com.gatalinka.app.ui.shell

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Book
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import android.util.Log
import androidx.navigation.NavHostController
import com.gatalinka.app.data.UserPreferencesRepository
import com.gatalinka.app.nav.AppNavHost
import com.gatalinka.app.nav.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GatalinkaScaffold(
    nav: NavHostController,
    preferencesRepo: UserPreferencesRepository,
    startDestination: String
) {
    val currentRoute = nav.currentDestination?.route ?: startDestination
    val showBottomBar = currentRoute in listOf(
        Routes.Home,
        Routes.MyReadings,
        Routes.SchoolOfReading,
        Routes.DailyReading
    )
    
    // Debug log (samo u debug build-u)
    if (com.gatalinka.app.BuildConfig.DEBUG) {
        LaunchedEffect(currentRoute) {
            Log.d("GatalinkaScaffold", "Current route: $currentRoute, showBottomBar: $showBottomBar")
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = Color(0xFF2D1B4E), // Mystic Purple Medium
                    contentColor = Color(0xFFFFD700) // Mystic Gold
                ) {
                    NavigationBarItem(
                        selected = currentRoute == Routes.Home,
                        onClick = { 
                            nav.navigate(Routes.Home) { 
                                popUpTo(Routes.Home) { inclusive = true }
                            } 
                        },
                        icon = { 
                            Icon(
                                Icons.Filled.Home, 
                                contentDescription = "Početna",
                                tint = if (currentRoute == Routes.Home) 
                                    Color(0xFFFFD700) 
                                else 
                                    Color(0xFFEFE3D1).copy(alpha = 0.7f)
                            ) 
                        },
                        label = { 
                            Text(
                                "Početna",
                                color = if (currentRoute == Routes.Home) 
                                    Color(0xFFFFD700) 
                                else 
                                    Color(0xFFEFE3D1).copy(alpha = 0.7f)
                            ) 
                        }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Routes.MyReadings,
                        onClick = { nav.navigate(Routes.MyReadings) },
                        icon = { 
                            Icon(
                                Icons.Filled.Info, 
                                contentDescription = "Moja čitanja",
                                tint = if (currentRoute == Routes.MyReadings) 
                                    Color(0xFFFFD700) 
                                else 
                                    Color(0xFFEFE3D1).copy(alpha = 0.7f)
                            ) 
                        },
                        label = { 
                            Text(
                                "Moja čitanja",
                                color = if (currentRoute == Routes.MyReadings) 
                                    Color(0xFFFFD700) 
                                else 
                                    Color(0xFFEFE3D1).copy(alpha = 0.7f)
                            ) 
                        }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Routes.SchoolOfReading,
                        onClick = { nav.navigate(Routes.SchoolOfReading) },
                        icon = { 
                            Icon(
                                Icons.Filled.Book, 
                                contentDescription = "Škola",
                                tint = if (currentRoute == Routes.SchoolOfReading) 
                                    Color(0xFFFFD700) 
                                else 
                                    Color(0xFFEFE3D1).copy(alpha = 0.7f)
                            ) 
                        },
                        label = { 
                            Text(
                                "Škola",
                                color = if (currentRoute == Routes.SchoolOfReading) 
                                    Color(0xFFFFD700) 
                                else 
                                    Color(0xFFEFE3D1).copy(alpha = 0.7f)
                            ) 
                        }
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            com.gatalinka.app.ui.components.MysticBackground {
                AppNavHost(nav, preferencesRepo, startDestination)
            }
        }
    }
}
