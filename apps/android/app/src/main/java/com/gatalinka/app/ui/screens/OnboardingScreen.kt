package com.gatalinka.app.ui.screens

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gatalinka.app.data.Gender
import com.gatalinka.app.data.UserPreferencesRepository
import com.gatalinka.app.ui.design.BeanCTA
import com.gatalinka.app.ui.design.GataUI
import com.gatalinka.app.util.DateValidators
import com.gatalinka.app.util.DobFormatter
import com.gatalinka.app.util.ZodiacSign
import com.gatalinka.app.vm.OnboardingViewModel
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    preferencesRepo: UserPreferencesRepository,
    vm: OnboardingViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onComplete: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    // Učitaj postojeće podatke iz DataStore
    val savedUserInput by preferencesRepo.userInput.collectAsState(initial = com.gatalinka.app.data.UserInput())
    
    var birth by remember {
        mutableStateOf(TextFieldValue(savedUserInput.birthdate))
    }
    var gender by remember { mutableStateOf(savedUserInput.gender) }
    var showDisclaimer by remember { mutableStateOf(true) }

    // Ažuriraj kada se učitaju podaci
    LaunchedEffect(savedUserInput.birthdate) {
        if (savedUserInput.birthdate.isNotEmpty() && birth.text != savedUserInput.birthdate) {
            birth = TextFieldValue(savedUserInput.birthdate)
            gender = savedUserInput.gender
        }
    }

    val isDateValid = remember(birth.text) { 
        DateValidators.isValidDob(birth.text)
    }
    val zodiac = remember(birth.text) { 
        if (isDateValid) com.gatalinka.app.util.ZodiacCalculator.calculateZodiac(birth.text) else null 
    }
    val isReady = isDateValid && gender != Gender.Unspecified
    
    // Sakrij tipkovnicu kada je datum validan i ima 10 znakova
    LaunchedEffect(isDateValid, birth.text.length) {
        if (isDateValid && birth.text.length == 10) {
            kotlinx.coroutines.delay(300) // Kratka pauza da korisnik vidi validaciju
            keyboardController?.hide()
        }
    }
    
    // Sakrij tipkovnicu kada se odabere spol
    LaunchedEffect(gender) {
        if (gender != Gender.Unspecified) {
            keyboardController?.hide()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = GataUI.ScreenPadding)
            .verticalScroll(rememberScrollState())
            .pointerInput(Unit) {
                // Sakrij tipkovnicu kada korisnik klikne negdje drugdje
                detectTapGestures {
                    keyboardController?.hide()
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "☕ Gatalinka",
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )
            
            Text(
                text = "AI gatanje iz šalice kave",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            )
            
            // Kratak intro
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Kako koristiti?",
                        style = MaterialTheme.typography.titleSmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                    Text(
                        "1. Fotkaj šalicu kave odozgo\n2. AI će analizirati simbole\n3. Pročitaj svoju sudbinu!",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Disclaimer
            if (showDisclaimer) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Važna napomena",
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        )
                        Text(
                            "Gatalinka je isključivo zabavnog karaktera. Ne donositi važne odluke na temelju rezultata.",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Form fields
            OutlinedTextField(
                value = birth,
                onValueChange = { tf ->
                    val (txt, pos) = DobFormatter.formatKeepingCursor(
                        tf.text,
                        tf.selection.end
                    )
                    birth = TextFieldValue(
                        txt,
                        TextRange(pos)
                    )
                    vm.updateBirthdate(txt)
                },
                label = { Text("Datum rođenja (DD.MM.GGGG)") },
                isError = birth.text.isNotBlank() && birth.text.length >= 10 && !isDateValid,
                supportingText = {
                    if (birth.text.isNotBlank() && birth.text.length >= 10 && !isDateValid) {
                        Text("Unesite ispravan datum, npr. 05.11.1990")
                    } else if (zodiac != null) {
                        Text("${zodiac.emoji} ${zodiac.displayName}")
                    } else if (birth.text.isNotBlank() && birth.text.length < 10) {
                        Text("Unesite datum u formatu DD.MM.GGGG")
                    }
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
                    .padding(bottom = 16.dp)
            )

            GenderSelector(
                selected = gender,
                onSelect = { g ->
                    keyboardController?.hide()
                    gender = g
                    vm.updateGender(g)
                },
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
            )
        }

        // Bottom CTA
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BeanCTA(
                label = if (isReady) "Započni gatanje" else "Popuni sve",
                onClick = {
                    keyboardController?.hide()
                    if (isReady) {
                        vm.acceptDisclaimer()
                        // Spremi podatke u DataStore
                        scope.launch {
                            preferencesRepo.saveUserInput(
                                com.gatalinka.app.data.UserInput(
                                    birthdate = birth.text,
                                    gender = gender,
                                    zodiacSign = zodiac,
                                    acceptedDisclaimer = true
                                )
                            )
                        }
                        onComplete()
                    }
                }
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

