package com.gatalinka.app.ui.screens

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.gatalinka.app.ui.components.MysticBackground
import com.gatalinka.app.ui.design.BeanCTA
import com.gatalinka.app.ui.design.GataUI
import com.gatalinka.app.util.DateValidators
import com.gatalinka.app.util.DobFormatter
import com.gatalinka.app.vm.ReadingForOthersViewModel

@Composable
fun ReadingForOthersScreen(
    vm: ReadingForOthersViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        key = "reading_for_others_shared"
    ),
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    
    var name by remember { mutableStateOf(TextFieldValue(vm.personName)) }
    var birth by remember { mutableStateOf(TextFieldValue(vm.customUserInput?.birthdate ?: "")) }
    var gender by remember { mutableStateOf(vm.customUserInput?.gender ?: Gender.Unspecified) }
    
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
            kotlinx.coroutines.delay(300)
            keyboardController?.hide()
        }
    }
    
    // Sakrij tipkovnicu kada se odabere spol
    LaunchedEffect(gender) {
        if (gender != Gender.Unspecified) {
            keyboardController?.hide()
        }
    }
    
    MysticBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = GataUI.ScreenPadding)
                .verticalScroll(rememberScrollState())
                .pointerInput(Unit) {
                    detectTapGestures {
                        keyboardController?.hide()
                    }
                }
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Nazad",
                        tint = androidx.compose.ui.graphics.Color(0xFFFFD700)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Gatanje za druge",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = androidx.compose.ui.graphics.Color(0xFFFFD700)
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Info card
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = androidx.compose.ui.graphics.Color(0xFF2D1B4E).copy(alpha = 0.8f)
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Unesite podatke osobe za koju želite gatanje",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = androidx.compose.ui.graphics.Color(0xFFFFD700),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        "Nakon unosa podataka, fotkajte šalicu kave za tu osobu.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = androidx.compose.ui.graphics.Color(0xFFEFE3D1).copy(alpha = 0.8f)
                    )
                }
            }
            
            // Form fields
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Ime (opcionalno)
                OutlinedTextField(
                    value = name,
                    onValueChange = { tf ->
                        name = tf
                        vm.updateName(tf.text)
                    },
                    label = { Text("Ime (opcionalno)") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = androidx.compose.ui.graphics.Color(0xFFEFE3D1),
                        unfocusedTextColor = androidx.compose.ui.graphics.Color(0xFFEFE3D1).copy(alpha = 0.7f),
                        focusedBorderColor = androidx.compose.ui.graphics.Color(0xFFFFD700),
                        unfocusedBorderColor = androidx.compose.ui.graphics.Color(0xFFFFD700).copy(alpha = 0.5f)
                    )
                )
                
                // Datum rođenja
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
                    label = { Text("Datum rođenja (DD.MM.GGGG) *") },
                    isError = birth.text.isNotBlank() && birth.text.length >= 10 && !isDateValid,
                    supportingText = {
                        if (birth.text.isNotBlank() && birth.text.length >= 10 && !isDateValid) {
                            Text(
                                "Unesite ispravan datum, npr. 05.11.1990",
                                color = androidx.compose.ui.graphics.Color(0xFFFF6B6B)
                            )
                        } else if (zodiac != null) {
                            Text("${zodiac.emoji} ${zodiac.displayName}")
                        } else if (birth.text.isNotBlank() && birth.text.length < 10) {
                            Text("Unesite datum u formatu DD.MM.GGGG")
                        } else if (birth.text.isEmpty()) {
                            Text("Datum je obavezan", color = androidx.compose.ui.graphics.Color(0xFFFF6B6B))
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
                        .padding(bottom = 16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = androidx.compose.ui.graphics.Color(0xFFEFE3D1),
                        unfocusedTextColor = androidx.compose.ui.graphics.Color(0xFFEFE3D1).copy(alpha = 0.7f),
                        focusedBorderColor = androidx.compose.ui.graphics.Color(0xFFFFD700),
                        unfocusedBorderColor = androidx.compose.ui.graphics.Color(0xFFFFD700).copy(alpha = 0.5f)
                    )
                )
                
                // Spol
                Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    GenderSelector(
                        selected = gender,
                        onSelect = { g ->
                            keyboardController?.hide()
                            gender = g
                            vm.updateGender(g)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (gender == Gender.Unspecified) {
                        Text(
                            text = "Spol je obavezan",
                            style = MaterialTheme.typography.bodySmall,
                            color = androidx.compose.ui.graphics.Color(0xFFFF6B6B),
                            modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // CTA Button - disabled ako nije sve ispravno
            BeanCTA(
                label = if (isReady) "Fotkaj šalicu" else "Popuni sve",
                onClick = {
                    keyboardController?.hide()
                    if (isReady) {
                        // VAŽNO: Osiguraj da se customUserInput postavi prije navigacije
                        // Osiguraj da su svi podaci postavljeni u ViewModelu
                        if (zodiac != null && isDateValid) {
                            // Osiguraj da je zodiac postavljen (možda je već postavljen, ali osiguraj)
                            vm.updateBirthdate(birth.text)
                        }
                        // Osiguraj da je gender postavljen
                        vm.updateGender(gender)
                        // Osiguraj da je ime postavljeno
                        vm.updateName(name.text)
                        
                        // Provjeri da li je sve postavljeno
                        val finalCustomInput = vm.customUserInput
                        if (com.gatalinka.app.BuildConfig.DEBUG) {
                            android.util.Log.d("ReadingForOthersScreen", "=== Navigating to CupEditor ===")
                            android.util.Log.d("ReadingForOthersScreen", "customUserInput != null: ${finalCustomInput != null}")
                            finalCustomInput?.let { cui ->
                                android.util.Log.d("ReadingForOthersScreen", "✅ zodiacSign: ${cui.zodiacSign?.displayName}")
                                android.util.Log.d("ReadingForOthersScreen", "✅ gender: ${cui.gender.name}")
                                android.util.Log.d("ReadingForOthersScreen", "✅ birthdate: ${cui.birthdate}")
                                android.util.Log.d("ReadingForOthersScreen", "✅ personName: ${vm.personName}")
                            } ?: run {
                                android.util.Log.e("ReadingForOthersScreen", "❌ customUserInput is NULL!")
                            }
                            android.util.Log.d("ReadingForOthersScreen", "=== End Navigation Check ===")
                        }
                        
                        // Provjeri da li je sve ispravno postavljeno prije navigacije
                        if (finalCustomInput == null || 
                            finalCustomInput.zodiacSign == null || 
                            finalCustomInput.gender == com.gatalinka.app.data.Gender.Unspecified ||
                            finalCustomInput.birthdate.isEmpty()) {
                            if (com.gatalinka.app.BuildConfig.DEBUG) {
                                android.util.Log.e("ReadingForOthersScreen", "❌ ERROR: customUserInput is incomplete! Not navigating.")
                            }
                            return@BeanCTA
                        }
                        
                        onContinue()
                    }
                },
                enabled = isReady,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 40.dp)
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
            color = androidx.compose.ui.graphics.Color(0xFFFFD700),
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
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = androidx.compose.ui.graphics.Color(0xFFFFD700),
            selectedLabelColor = androidx.compose.ui.graphics.Color(0xFF1A0B2E),
            containerColor = androidx.compose.ui.graphics.Color(0xFF2D1B4E).copy(alpha = 0.6f),
            labelColor = androidx.compose.ui.graphics.Color(0xFFEFE3D1)
        )
    )
}

