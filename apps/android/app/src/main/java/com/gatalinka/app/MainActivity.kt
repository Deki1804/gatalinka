package com.gatalinka.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.compose.rememberNavController
import com.gatalinka.app.data.UserPreferencesRepository
import com.gatalinka.app.ui.shell.GatalinkaScaffold
import com.gatalinka.app.ui.theme.GatalinkaTheme
import com.gatalinka.app.util.DateValidators
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.flow.first

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

val LocalAudioEngine = compositionLocalOf<com.gatalinka.app.core.audio.AudioEngine> { error("No AudioEngine provided") }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GatalinkaTheme {
                val systemUiController = rememberSystemUiController()
                val bgColor = MaterialTheme.colorScheme.background
                val darkIcons = false

                // Audio Engine
                val context = androidx.compose.ui.platform.LocalContext.current
                val audioEngine = remember { com.gatalinka.app.core.audio.AudioEngine(context) }
                
                // Start background music (TODO: Add mystic_bg.mp3 to res/raw)
                // LaunchedEffect(Unit) {
                //     audioEngine.playBackgroundMusic(R.raw.mystic_bg)
                // }

                SideEffect {
                    systemUiController.setSystemBarsColor(
                        color = androidx.compose.ui.graphics.Color.Transparent,
                        darkIcons = darkIcons
                    )
                    systemUiController.setNavigationBarColor(
                        color = androidx.compose.ui.graphics.Color.Transparent,
                        darkIcons = darkIcons
                    )
                }

                val nav = rememberNavController()
                val preferencesRepo = remember { UserPreferencesRepository(dataStore) }
                
                // Provjeri auth state i onboarding status
                val authViewModel: com.gatalinka.app.vm.AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
                val authState by authViewModel.authState.collectAsState()
                val hasCompletedOnboarding by preferencesRepo.hasCompletedOnboarding.collectAsState(initial = false)
                
                // Odredi start destination
                val startDestination = remember(authState, hasCompletedOnboarding) {
                    when {
                        authState !is com.gatalinka.app.vm.AuthState.Authenticated -> {
                            com.gatalinka.app.nav.Routes.Login
                        }
                        !hasCompletedOnboarding -> {
                            com.gatalinka.app.nav.Routes.Onboarding
                        }
                        else -> {
                            com.gatalinka.app.nav.Routes.Home
                        }
                    }
                }

                CompositionLocalProvider(com.gatalinka.app.LocalAudioEngine provides audioEngine) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = bgColor
                    ) {
                        GatalinkaScaffold(nav, preferencesRepo, startDestination)
                    }
                }
            }
        }
    }
}
