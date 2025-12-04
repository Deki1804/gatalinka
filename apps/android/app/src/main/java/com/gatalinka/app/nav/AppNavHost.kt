package com.gatalinka.app.nav

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.gatalinka.app.data.UserPreferencesRepository
import com.gatalinka.app.ui.screens.*
import com.gatalinka.app.ui.components.LoadingScreen
import com.gatalinka.app.util.ErrorMessages

@Composable
fun AppNavHost(
    nav: NavHostController,
    preferencesRepo: UserPreferencesRepository,
    startDestination: String
) {
    NavHost(navController = nav, startDestination = startDestination) {
        composable(Routes.Home) {
            HomeScreen(
                onReadCup = { nav.navigate(Routes.ReadingModeSelection) },
                onMyReadings = { nav.navigate(Routes.MyReadings) },
                onSchool = { nav.navigate(Routes.SchoolOfReading) },
                onDailyReading = { nav.navigate(Routes.DailyReading) },
                onLogin = { nav.navigate(Routes.Login) },
                onProfile = { nav.navigate(Routes.Profile) },
                onReadingForOthers = { nav.navigate(Routes.ReadingForOthers) },
                preferencesRepo = preferencesRepo
            )
        }
        composable(Routes.ReadingForOthers) {
            // ViewModel za custom UserInput
            val readingForOthersVm: com.gatalinka.app.vm.ReadingForOthersViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                key = "reading_for_others_shared"
            )
            ReadingForOthersScreen(
                vm = readingForOthersVm,
                onBack = { 
                    // Očisti podatke ako korisnik ode nazad
                    readingForOthersVm.clear()
                    nav.popBackStack() 
                },
                onContinue = { nav.navigate(Routes.CupEditor) }
            )
        }
        composable(Routes.Login) {
            val hasCompletedOnboarding by preferencesRepo.hasCompletedOnboarding.collectAsState(initial = false)
            
            LoginScreen(
                onBack = { 
                    // Ne može ići nazad ako nije prijavljen
                    // Ako je prijavljen, već bi trebao biti na Home
                },
                onLoginSuccess = {
                    // Provjeri da li je korisnik završio onboarding
                    if (hasCompletedOnboarding) {
                        // Ako je završio onboarding, idi na Home
                        nav.navigate(Routes.Home) {
                            popUpTo(Routes.Login) { inclusive = true }
                        }
                    } else {
                        // Ako nije završio onboarding, idi na Onboarding
                        nav.navigate(Routes.Onboarding) {
                            popUpTo(Routes.Login) { inclusive = true }
                        }
                    }
                },
                onNavigateToRegister = { nav.navigate(Routes.Register) }
            )
        }
        composable(Routes.Register) {
            RegisterScreen(
                onBack = { nav.popBackStack() },
                onRegisterSuccess = {
                    // Nakon registracije, idi na Onboarding za unos datuma i spola
                    nav.popBackStack()
                    nav.navigate(Routes.Onboarding)
                },
                onNavigateToLogin = { nav.navigate(Routes.Login) }
            )
        }
        composable(Routes.Onboarding) {
            OnboardingFlowScreen(
                preferencesRepo = preferencesRepo,
                onComplete = {
                    // Nakon onboardinga, idi na Home
                    nav.navigate(Routes.Home) {
                        popUpTo(Routes.Login) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.ReadingModeSelection) { backStackEntry ->
            ReadingModeSelectionScreen(
                onBack = { nav.popBackStack() },
                onModeSelected = { readingMode ->
                    // Spremi readingMode u savedStateHandle i idi na CupEditor
                    backStackEntry.savedStateHandle["readingMode"] = readingMode
                    nav.navigate(Routes.CupEditor)
                }
            )
        }
        composable(Routes.CupEditor) { backStackEntry ->
            // Dohvati readingMode iz savedStateHandle
            val readingMode = remember {
                backStackEntry.savedStateHandle.get<String>("readingMode") ?: "instant"
            }
            CupEditorScreen(
                onBack = { nav.popBackStack() },
                onAnalyze = { imageUri, selectedMode ->
                    // Koristi selectedMode iz CupEditorScreen (može se promijeniti)
                    val encodedUri = java.net.URLEncoder.encode(imageUri, "UTF-8")
                    val encodedMode = java.net.URLEncoder.encode(selectedMode, "UTF-8")
                    nav.navigate("${Routes.ReadCup}/$encodedUri/$encodedMode")
                },
                initialReadingMode = readingMode
            )
        }
        composable("${Routes.ReadCup}/{imageUri}/{readingMode}") { backStackEntry ->
            val encodedUri = backStackEntry.arguments?.getString("imageUri") ?: ""
            val encodedMode = backStackEntry.arguments?.getString("readingMode") ?: "instant"
            val imageUri = try {
                java.net.URLDecoder.decode(encodedUri, "UTF-8")
            } catch (e: Exception) {
                encodedUri
            }
            val readingMode = try {
                java.net.URLDecoder.decode(encodedMode, "UTF-8")
            } catch (e: Exception) {
                "instant"
            }
            // ViewModel za custom UserInput - koristi isti key kao u ReadCupScreen
            val readingForOthersVm: com.gatalinka.app.vm.ReadingForOthersViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                key = "reading_for_others_shared"
            )
            ReadCupScreen(
                imageUri = imageUri,
                readingMode = readingMode,
                preferencesRepo = preferencesRepo,
                onBack = { 
                    // Očisti custom UserInput ako postoji
                    readingForOthersVm.clear()
                    // Navigiraj nazad - ako je došao iz ReadingForOthers, vrati se tamo, inače na Home
                    if (nav.previousBackStackEntry?.destination?.route == Routes.ReadingForOthers) {
                        nav.popBackStack()
                    } else {
                        nav.navigate(Routes.Home) {
                            popUpTo(Routes.Home) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                },
                onViewResult = { result ->
                    if (com.gatalinka.app.BuildConfig.DEBUG) {
                        android.util.Log.d("AppNavHost", "onViewResult pozvan! luckScore=${result.luckScore}")
                    }
                    // Spremi rezultat u savedStateHandle trenutnog ekrana
                    backStackEntry.savedStateHandle["readingResult"] = result
                    // Spremi i ime ako postoji (za gatanje drugim ljudima)
                    val targetName = readingForOthersVm.personName.takeIf { it.isNotEmpty() }
                    backStackEntry.savedStateHandle["targetName"] = targetName
                    
                    // Navigiraj na ReadingResult s readingMode
                    val encodedResultUri = java.net.URLEncoder.encode(imageUri, "UTF-8")
                    val encodedResultMode = java.net.URLEncoder.encode(readingMode, "UTF-8")
                    nav.navigate("${Routes.ReadingResult}/$encodedResultUri/$encodedResultMode")
                }
            )
        }
        composable("${Routes.ReadingResult}/{imageUri}/{readingMode}") { backStackEntry ->
            val encodedUri = backStackEntry.arguments?.getString("imageUri") ?: ""
            val encodedMode = backStackEntry.arguments?.getString("readingMode") ?: "instant"
            val resultImageUri = try {
                java.net.URLDecoder.decode(encodedUri, "UTF-8")
            } catch (e: Exception) {
                encodedUri
            }
            val resultReadingMode = try {
                java.net.URLDecoder.decode(encodedMode, "UTF-8")
            } catch (e: Exception) {
                "instant"
            }
            
            // Čitaj rezultat iz savedStateHandle prethodnog ekrana
            val savedStateHandle = nav.previousBackStackEntry?.savedStateHandle
            var result by remember { mutableStateOf<com.gatalinka.app.ui.model.GatalinkaReadingUiModel?>(null) }
            
            // Pokušaj dohvatiti rezultat
            LaunchedEffect(Unit) {
                if (com.gatalinka.app.BuildConfig.DEBUG) {
                    android.util.Log.d("AppNavHost", "ReadingResultScreen: Pokušavam dohvatiti rezultat iz savedStateHandle")
                }
                val readingResult = savedStateHandle?.get<com.gatalinka.app.ui.model.GatalinkaReadingUiModel>("readingResult")
                result = readingResult
                if (readingResult == null) {
                    if (com.gatalinka.app.BuildConfig.DEBUG) {
                        android.util.Log.e("AppNavHost", "❌ Rezultat nije pronađen! Vraćam se nazad.")
                    }
                    // Ako nema rezultata, vrati se nazad
                    nav.popBackStack()
                }
            }
            
            val currentResult = result
            // ViewModel za custom UserInput - očisti nakon čitanja
            val readingForOthersVm: com.gatalinka.app.vm.ReadingForOthersViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                key = "reading_for_others_shared"
            )
            // Dohvati ime iz savedStateHandle
            var targetName by remember { mutableStateOf<String?>(null) }
            LaunchedEffect(Unit) {
                val savedStateHandle = nav.previousBackStackEntry?.savedStateHandle
                targetName = savedStateHandle?.get<String>("targetName")
            }
            if (currentResult != null) {
                ReadingResultScreen(
                    result = currentResult,
                    imageUri = resultImageUri,
                    onBack = { 
                        // Očisti custom UserInput
                        readingForOthersVm.clear()
                        // Navigiraj direktno na Home i očisti cijeli back stack
                        nav.navigate(Routes.Home) {
                            // Pop to start destination (Home) and clear everything
                            popUpTo(nav.graph.startDestinationId) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onSave = { 
                        // Očisti custom UserInput
                        readingForOthersVm.clear()
                        // Navigiraj direktno na Home i očisti cijeli back stack
                        nav.navigate(Routes.Home) {
                            // Pop to start destination (Home) and clear everything
                            popUpTo(nav.graph.startDestinationId) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    targetName = targetName
                )
            } else {
                // Prikaži loading dok čekamo rezultat
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.CircularProgressIndicator()
                }
            }
        }
        composable(Routes.MyReadings) {
            MyReadingsScreen(
                onBack = { nav.popBackStack() },
                onReadingClick = { readingId ->
                    nav.navigate("${Routes.SavedReading}/$readingId")
                },
                onNewReading = { nav.navigate(Routes.CupEditor) }
            )
        }
        composable("${Routes.SavedReading}/{readingId}") { backStackEntry ->
            val readingId = backStackEntry.arguments?.getString("readingId") ?: ""
            val readingsRepo = remember { com.gatalinka.app.data.CloudReadingsRepository() }
            var reading by remember { mutableStateOf<com.gatalinka.app.data.CupReading?>(null) }
            var isLoading by remember { mutableStateOf(true) }
            var error by remember { mutableStateOf<String?>(null) }
            
            LaunchedEffect(readingId) {
                isLoading = true
                error = null
                try {
                    reading = readingsRepo.getReadingById(readingId)
                    if (reading == null) {
                        error = "Čitanje nije pronađeno"
                    }
                } catch (e: Exception) {
                    error = com.gatalinka.app.util.ErrorMessages.getErrorMessage(e)
                    if (com.gatalinka.app.BuildConfig.DEBUG) {
                        android.util.Log.e("AppNavHost", "Error loading reading", e)
                    }
                } finally {
                    isLoading = false
                }
            }
            
            when {
                isLoading -> {
                    LoadingScreen(message = "Učitavam čitanje...")
                }
                error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            com.gatalinka.app.ui.components.ErrorCard(
                                message = error ?: ErrorMessages.UNKNOWN_ERROR,
                                onRetry = null
                            )
                            Spacer(Modifier.height(16.dp))
                            androidx.compose.material3.Button(
                                onClick = { nav.popBackStack() }
                            ) {
                                androidx.compose.material3.Text("Nazad")
                            }
                        }
                    }
                }
                reading != null -> {
                    val uiModel = com.gatalinka.app.data.ReadingMapper.mapToUiModel(reading!!)
                    ReadingResultScreen(
                        result = uiModel,
                        imageUri = reading!!.imageUri,
                        onBack = { nav.popBackStack() },
                        onSave = { nav.popBackStack() }
                    )
                }
            }
        }
        composable(Routes.SchoolOfReading) {
            SchoolOfReadingScreen(
                onBack = { nav.popBackStack() }
            )
        }
        composable(Routes.DailyReading) {
            DailyReadingScreen(
                preferencesRepo = preferencesRepo,
                onBack = { nav.popBackStack() }
            )
        }
        composable(Routes.Profile) {
            ProfileScreen(
                onBack = { nav.popBackStack() },
                onSettings = { nav.navigate(Routes.Settings) }
            )
        }
        composable(Routes.Settings) {
            SettingsScreen(
                onBack = { nav.popBackStack() },
                onSignOut = {
                    // Nakon odjave, idi na Login i očisti back stack
                    nav.navigate(Routes.Login) {
                        popUpTo(Routes.Login) { inclusive = true }
                    }
                },
                navController = nav
            )
        }
        composable(Routes.PrivacyPolicy) {
            PrivacyPolicyScreen(
                onBack = { nav.popBackStack() }
            )
        }
        composable(Routes.TermsOfUse) {
            TermsOfUseScreen(
                onBack = { nav.popBackStack() }
            )
        }
    }
}
