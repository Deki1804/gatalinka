package com.gatalinka.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import android.view.HapticFeedbackConstants
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.activity.compose.BackHandler
import com.gatalinka.app.ui.components.MysticBackground
import com.gatalinka.app.ui.components.ReadingGlowEffect
import com.gatalinka.app.ui.components.MysticOrb
import com.gatalinka.app.util.ErrorMessages
import com.gatalinka.app.ui.design.GataUI

/**
 * Generira tekst format direktno iz simbola.
 * Za čitanje iz šalice: "U tvojoj šalici se vidi [simbol1], [simbol2] i [simbol3]..."
 * Za daily reading: "Danas te prate simboli: [simbol1], [simbol2] i [simbol3]..."
 */
private fun generateWhatSeenInCupFromSymbols(
    symbols: List<com.gatalinka.app.ui.model.VisibleSymbol>?,
    isDailyReading: Boolean = false
): String? {
    if (symbols.isNullOrEmpty()) return null
    
    val symbolNames = symbols.map { it.symbol }
    
    // Prvi dio: različito za daily reading vs čitanje iz šalice
    val firstPart = if (isDailyReading) {
        // Daily reading: "Danas te prate simboli: X, Y i Z."
        when (symbolNames.size) {
            1 -> "Danas te prate simboli: ${symbolNames.first()}."
            2 -> "Danas te prate simboli: ${symbolNames[0]} i ${symbolNames[1]}."
            else -> {
                val last = symbolNames.last()
                val others = symbolNames.dropLast(1).joinToString(", ")
                "Danas te prate simboli: $others i $last."
            }
        }
    } else {
        // Čitanje iz šalice: "U tvojoj šalici se vidi X, Y i Z."
        when (symbolNames.size) {
            1 -> "U tvojoj šalici se vidi ${symbolNames.first()}."
            2 -> "U tvojoj šalici se vidi ${symbolNames[0]} i ${symbolNames[1]}."
            else -> {
                val last = symbolNames.last()
                val others = symbolNames.dropLast(1).joinToString(", ")
                "U tvojoj šalici se vidi $others i $last."
            }
        }
    }
    
    // Drugi dio: kratka značenja simbola (bapski stil, direktno)
    val meanings = symbols.mapNotNull { symbol ->
        val meaning = symbol.meaning.takeIf { 
            it.isNotBlank() && 
            it != "Simbol u šalici" &&
            it.length < 50 && // Kratka značenja
            !it.contains("horoskop", ignoreCase = true) &&
            !it.contains("energija", ignoreCase = true) &&
            !it.contains("more", ignoreCase = true) &&
            !it.contains("oblak", ignoreCase = true) &&
            !it.contains("vatra", ignoreCase = true)
        } ?: return@mapNotNull null
        
        // Formatiraj u bapski stil: "Ključ pokazuje rješenje ili izlaz."
        when {
            meaning.startsWith(symbol.symbol, ignoreCase = true) -> meaning
            meaning.length < 25 -> "${symbol.symbol} znači $meaning."
            else -> "${symbol.symbol} pokazuje $meaning."
        }
    }
    
    return if (meanings.isNotEmpty()) {
        "$firstPart ${meanings.joinToString(" ")}"
    } else {
        firstPart
    }
}

/**
 * Filtrira horoskopski jezik iz teksta.
 */
private fun filterHoroscopeLanguage(text: String): String {
    var filtered = text
    // Ukloni reference na horoskop znakove
    val zodiacSigns = listOf("Ovan", "Lav", "Blizanci", "Rak", "Vaga", "Škorpion", 
        "Strijelac", "Jarac", "Vodenjak", "Ribe", "Bik", "horoskop", "zodijak")
    zodiacSigns.forEach { sign ->
        filtered = filtered.replace(sign, "", ignoreCase = true)
    }
    // Ukloni generičke astro fraze
    val astroPhrases = listOf(
        "energija dana", "unutarnja vatra", "univerzalna energija",
        "kozmička energija", "astro energija", "energija zodiaka"
    )
    astroPhrases.forEach { phrase ->
        filtered = filtered.replace(phrase, "", ignoreCase = true)
    }
    // Očisti višestruke razmake
    filtered = filtered.replace(Regex("\\s+"), " ").trim()
    return filtered
}

/**
 * Generira horoskopsku potvrdu koja se referira na simbole iz šalice.
 * Primjer: "Kao Ovan, poznat si po hrabrosti. Ono što se vidi u šalici slaže se s tim — ali ovoga puta bolje je stati i razmisliti."
 */
private fun generateHoroscopeConfirmation(
    zodiacSign: com.gatalinka.app.util.ZodiacSign?,
    symbols: List<com.gatalinka.app.ui.model.VisibleSymbol>?
): String? {
    if (zodiacSign == null || symbols.isNullOrEmpty()) return null
    
    val zodiacName = zodiacSign.displayName
    val symbolNames = symbols.take(3).map { it.symbol }.joinToString(", ")
    
    // Karakteristike znakova (kratko, bapski stil)
    val zodiacTraits = when (zodiacSign) {
        com.gatalinka.app.util.ZodiacSign.Aries -> "hrabrosti i brzini odluka"
        com.gatalinka.app.util.ZodiacSign.Taurus -> "upornosti i strpljivosti"
        com.gatalinka.app.util.ZodiacSign.Gemini -> "komunikaciji i promjenama"
        com.gatalinka.app.util.ZodiacSign.Cancer -> "osjećajnosti i domoljublju"
        com.gatalinka.app.util.ZodiacSign.Leo -> "samopouzdanju i velikodušnosti"
        com.gatalinka.app.util.ZodiacSign.Virgo -> "pažnji na detalje i praktičnosti"
        com.gatalinka.app.util.ZodiacSign.Libra -> "ravnoteži i diplomaciji"
        com.gatalinka.app.util.ZodiacSign.Scorpio -> "intenzitetu i dubini"
        com.gatalinka.app.util.ZodiacSign.Sagittarius -> "slobodi i avanturi"
        com.gatalinka.app.util.ZodiacSign.Capricorn -> "ambiciji i odgovornosti"
        com.gatalinka.app.util.ZodiacSign.Aquarius -> "nezavisnosti i inovativnosti"
        com.gatalinka.app.util.ZodiacSign.Pisces -> "intuiciji i osjećajnosti"
    }
    
    return "Kao $zodiacName, poznat si po $zodiacTraits. Ono što se vidi u šalici ($symbolNames) slaže se s tim — ali ovoga puta bolje je stati i razmisliti prije nego kreneš."
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingResultScreen(
    result: com.gatalinka.app.ui.model.GatalinkaReadingUiModel,
    imageUri: String,
    onBack: () -> Unit,
    onSave: () -> Unit,
    targetName: String? = null, // Ime osobe za koju je gatanje (null = za sebe)
    preferencesRepo: com.gatalinka.app.data.UserPreferencesRepository? = null, // Opcionalno za horoskop potvrdu
    onRetryFromGallery: (() -> Unit)? = null // Callback za retry iz galerije
) {
    val context = LocalContext.current
    val view = LocalView.current
    val coroutineScope = rememberCoroutineScope()
    val readingsRepo = remember { com.gatalinka.app.data.CloudReadingsRepository() }
    
    // Dohvati zodiac sign za horoskop potvrdu
    val userInput by preferencesRepo?.userInput?.collectAsState(initial = com.gatalinka.app.data.UserInput()) 
        ?: remember { mutableStateOf(com.gatalinka.app.data.UserInput()) }
    val zodiacSign = remember(userInput) { userInput.zodiacSign }
    
    var isSaving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }
    
    // Presretni sistem back button i pozovi naš onBack callback
    BackHandler(onBack = onBack)
    
    // Share functionality - viralno formatiranje za WhatsApp/Viber
    fun buildShareText(result: com.gatalinka.app.ui.model.GatalinkaReadingUiModel): String {
        val sb = StringBuilder()
        sb.append("GATALINKA ☕🔮\n")
        sb.append("Moje čitanje iz šalice kave\n\n")
        
        // Visible symbols with meanings (BAPSKI STIL) - najvažnije prvo
        if (!result.visibleSymbols.isNullOrEmpty()) {
            sb.append("U šalici se vidi:\n")
            result.visibleSymbols.take(5).forEach { symbol ->
                sb.append("• ${symbol.symbol} — ${symbol.meaning}\n")
            }
            sb.append("\n")
        } else if (result.symbols.isNotEmpty()) {
            sb.append("U šalici se vidi:\n")
            result.symbols.take(5).forEach { symbol ->
                sb.append("• $symbol\n")
            }
            sb.append("\n")
        }
        
        // Interpretation - kako se to tumači
        if (!result.interpretation.isNullOrEmpty()) {
            sb.append("Tumačenje:\n")
            sb.append("${result.interpretation}\n\n")
        } else if (result.mainText.isNotEmpty()) {
            sb.append("Tumačenje:\n")
            sb.append("${result.mainText}\n\n")
        }
        
        // Advice - kratki savjet
        if (!result.advice.isNullOrEmpty()) {
            sb.append("Savjet:\n")
            sb.append("${result.advice}\n\n")
        }
        
        // Kategorije (kratko)
        if (result.love?.isNotEmpty() == true) {
            sb.append("💕 ${result.love}\n")
        }
        if (result.work?.isNotEmpty() == true) {
            sb.append("💼 ${result.work}\n")
        }
        if (result.money?.isNotEmpty() == true) {
            sb.append("💰 ${result.money}\n")
        }
        if (result.health?.isNotEmpty() == true) {
            sb.append("🌿 ${result.health}\n")
        }
        
        if (result.love?.isNotEmpty() == true || result.work?.isNotEmpty() == true || 
            result.money?.isNotEmpty() == true || result.health?.isNotEmpty() == true) {
            sb.append("\n")
        }
        
        // Sretni brojevi
        if (result.luckyNumbers.isNotEmpty()) {
            sb.append("🎲 Sretni brojevi: ${result.luckyNumbers.joinToString(", ")}\n\n")
        }
        
        // Footer
        sb.append("Preuzmi Gatalinka app i otkrij svoju sudbinu! 🔮")
        return sb.toString()
    }
    
    fun shareReading() {
        val shareText = buildShareText(result)
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Dijeli čitanje")
        context.startActivity(shareIntent)
    }

    MysticBackground {
        // Additional glow effect behind header
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Subtle glow behind content
            ReadingGlowEffect(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.3f),
                centerX = null,
                centerY = null
            )
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
            // Top Bar
            TopAppBar(
                title = {
                    Column {
                        Text(
                            if (targetName != null) "Čitanje za: $targetName" else "Tvoja sudbina",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color(0xFFFFD700)
                        )
                        if (targetName != null) {
                            Text(
                                "Gatanje za drugu osobu",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFEFE3D1).copy(alpha = 0.7f)
                            )
                        }
                    }
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
                    TextButton(
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                            shareReading()
                        }
                    ) {
                        Icon(
                            Icons.Default.Share,
                            "Dijeli",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Podijeli",
                            color = Color(0xFFFFD700)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )

            // Content with entrance animation
            val entranceScale = remember { androidx.compose.animation.core.Animatable(0.9f) }
            LaunchedEffect(Unit) {
                entranceScale.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(800, easing = FastOutSlowInEasing)
                )
            }
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .navigationBarsPadding()
                    .padding(bottom = 100.dp) // Extra padding for bottom nav bar + button
                    .graphicsLayer {
                        scaleX = entranceScale.value
                        scaleY = entranceScale.value
                    }
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Stagger animations for cards - povećana lista za više kartica
                val cardDelays = listOf(0, 100, 200, 300, 400, 500, 600, 700, 800, 900, 1000, 1100)
                var cardIndex = 0
                
                // Helper funkcija za siguran pristup cardDelays
                fun getCardDelay(index: Int): Int {
                    return cardDelays.getOrElse(index) { cardDelays.last() }
                }
                
                // Luck Score Card with glassmorphism
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(durationMillis = 600, delayMillis = getCardDelay(cardIndex++))) +
                            slideInVertically(
                                initialOffsetY = { 30 },
                                animationSpec = tween(durationMillis = 600, delayMillis = getCardDelay(cardIndex - 1))
                            ) + scaleIn(
                                initialScale = 0.9f,
                                animationSpec = tween(durationMillis = 600, delayMillis = getCardDelay(cardIndex - 1))
                            )
                ) {
                    LuckScoreCard(result.luckScore)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Energy Score Card
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(durationMillis = 600, delayMillis = getCardDelay(cardIndex++))) +
                            slideInVertically(
                                initialOffsetY = { 30 },
                                animationSpec = tween(durationMillis = 600, delayMillis = getCardDelay(cardIndex - 1))
                            )
                ) {
                    EnergyScoreCard(result.energyScore)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // BAPSKI REDOSLIJED:
                // 1. Prepoznati simboli (izvor svega)
                // 2. Što se vidi u šalici (direktno iz simbola)
                // 3. Kako baba to tumači (interpretation)
                // 4. Savjet iz šalice (advice)

                // Provjeri da li je daily reading (nema slike) - definirati prije korištenja
                val isDailyReading = imageUri == "daily_reading_placeholder"

                // 1. PREPOZNATI SIMBOLI / SIMBOLI DANA - IZVOR SVEGA
                if (!result.visibleSymbols.isNullOrEmpty()) {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = tween(durationMillis = 600, delayMillis = getCardDelay(cardIndex++))) +
                                slideInVertically(
                                    initialOffsetY = { 30 },
                                    animationSpec = tween(durationMillis = 600, delayMillis = getCardDelay(cardIndex - 1))
                                )
                    ) {
                        VisibleSymbolsCard(result.visibleSymbols, isDailyReading)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 2. "ŠTO SE VIDI U ŠALICI" ili "PORUKA DANA" - DIREKTNO IZ SIMBOLA (konkretno, bez poezije)
                // Za daily reading, filtriraj "U tvojoj šalici" iz teksta
                val mainTextForDaily = if (isDailyReading && result.mainText.isNotBlank()) {
                    result.mainText
                        .replace(Regex("(?i)u tvojoj šalici se vidi"), "Danas te prate simboli:")
                        .replace(Regex("(?i)u šalici se vidi"), "Danas te prate simboli:")
                        .replace(Regex("(?i)u šalici"), "Danas")
                        .replace(Regex("(?i)šalici"), "danas")
                        .replace(Regex("(?i)talog"), "energija")
                        .replace(Regex("(?i)na dnu"), "danas")
                        .replace(Regex("(?i)uz rub"), "danas")
                        .replace(Regex("(?i)u sredini"), "danas")
                } else {
                    result.mainText
                }
                
                val whatSeenInCup = generateWhatSeenInCupFromSymbols(result.visibleSymbols, isDailyReading)
                    ?: result.interpretation?.takeIf { it.isNotBlank() }
                        ?.let { filterHoroscopeLanguage(it) }
                    ?: mainTextForDaily.takeIf { it.isNotBlank() }
                        ?.let { filterHoroscopeLanguage(it) }
                
                if (!whatSeenInCup.isNullOrBlank()) {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = tween(durationMillis = 600, delayMillis = getCardDelay(cardIndex++))) +
                                slideInVertically(
                                    initialOffsetY = { 30 },
                                    animationSpec = tween(durationMillis = 600, delayMillis = getCardDelay(cardIndex - 1))
                                )
                    ) {
                        ReadingCard(
                            title = if (isDailyReading) "Poruka dana" else "Što se vidi u šalici",
                            content = whatSeenInCup
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 3. "KAKO BABA TO TUMAČI" - interpretation (MORA referencirati simbole)
                val babaInterpretation = result.interpretation?.takeIf { it.isNotBlank() }
                    ?.let { filterHoroscopeLanguage(it) }
                    ?.takeIf { 
                        // Provjeri da nije isti kao "Što se vidi"
                        it != whatSeenInCup && 
                        it.length > 30 && // Minimalna duljina
                        // Provjeri da referencira simbole (ako postoje)
                        (result.visibleSymbols.isNullOrEmpty() || 
                         result.visibleSymbols.any { symbol -> 
                             it.contains(symbol.symbol, ignoreCase = true) 
                         })
                    }
                
                if (!babaInterpretation.isNullOrBlank()) {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = tween(durationMillis = 600, delayMillis = getCardDelay(cardIndex++))) +
                                slideInVertically(
                                    initialOffsetY = { 30 },
                                    animationSpec = tween(durationMillis = 600, delayMillis = getCardDelay(cardIndex - 1))
                                )
                    ) {
                        ReadingCard(
                            title = "Kako baba to tumači",
                            content = babaInterpretation
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 4. "SAVJET IZ ŠALICE" - advice (1-2 rečenice max, bapski stil)
                val babaAdvice = result.advice?.takeIf { it.isNotBlank() }
                    ?.let { filterHoroscopeLanguage(it) }
                    ?.takeIf { 
                        // Provjeri da nije isti kao druge sekcije
                        it != whatSeenInCup && 
                        it != babaInterpretation &&
                        it.length < 200 // Maksimalno 1-2 rečenice
                    }
                
                // Provjeri da li je reading neuspješan (nema simbola ili je error poruka)
                val isFailedReading = result.visibleSymbols.isNullOrEmpty() && 
                    (whatSeenInCup?.contains("Talog se još skriva", ignoreCase = true) == true ||
                     whatSeenInCup?.contains("Ne vidim", ignoreCase = true) == true ||
                     whatSeenInCup?.contains("Ovo nije", ignoreCase = true) == true)
                
                if (!babaAdvice.isNullOrBlank()) {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = tween(durationMillis = 600, delayMillis = getCardDelay(cardIndex++))) +
                                slideInVertically(
                                    initialOffsetY = { 30 },
                                    animationSpec = tween(durationMillis = 600, delayMillis = getCardDelay(cardIndex - 1))
                                )
                    ) {
                        ReadingCard(
                            title = if (isDailyReading) "Savjet dana" else "Savjet iz šalice",
                            content = babaAdvice
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // CTA gumbe za neuspješan reading
                if (isFailedReading) {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = tween(durationMillis = 600, delayMillis = getCardDelay(cardIndex++))) +
                                slideInVertically(
                                    initialOffsetY = { 30 },
                                    animationSpec = tween(durationMillis = 600, delayMillis = getCardDelay(cardIndex - 1))
                                )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Primary: Slikaj opet
                            Button(
                                onClick = {
                                    onBack() // Vrati na CupEditorScreen
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GataUI.MysticGold,
                                    contentColor = GataUI.MysticPurpleDeep
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    "📷 Slikaj opet",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            // Secondary: Iz galerije
                            OutlinedButton(
                                onClick = {
                                    // Ako postoji callback za retry iz galerije, koristi ga
                                    // Inače samo vrati se nazad
                                    if (onRetryFromGallery != null) {
                                        onRetryFromGallery()
                                    } else {
                                        onBack()
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = GataUI.MysticGold
                                ),
                                border = BorderStroke(2.dp, GataUI.MysticGold),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    "🖼️ Iz galerije",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Categories with animations - prikaži samo ako AI eksplicitno vrati BAPSKI stil
                // Sakrij sekcije koje sadrže horoskop/zodiac reference ili generičke astro fraze
                fun isBapskiContent(text: String?): Boolean {
                    if (text.isNullOrBlank()) return false
                    val filtered = filterHoroscopeLanguage(text)
                    if (filtered.length < 30) return false // Minimalna duljina
                    
                    val lowerText = filtered.lowercase()
                    // Provjeri da nije generički horoskop tekst
                    val hasHoroscopeKeywords = listOf(
                        "horoskop", "ovan", "lav", "blizanci", "rak", "vaga",
                        "škorpion", "strijelac", "jarac", "vodenjak", "ribe", "bik",
                        "energija dana", "unutarnja vatra", "kozmička", "astro"
                    ).any { lowerText.contains(it, ignoreCase = true) }
                    
                    if (hasHoroscopeKeywords) return false
                    
                    // Provjeri da je bapski stil - trebao bi spominjati simbole ili biti konkretan
                    val hasBapskiKeywords = listOf(
                        "u šalici", "vidi se", "pokazuje", "govori o", "znači",
                        "put", "srce", "knjiga", "cesta", "ptica", "sunce"
                    ).any { lowerText.contains(it, ignoreCase = true) }
                    
                    // Ako nema bapskih ključnih riječi, možda je još uvijek generički
                    // Ali ako je dovoljno dugačak i nema horoskop ključnih riječi, prikaži
                    return filtered.length > 50 || hasBapskiKeywords
                }
                
                // Helper funkcija za filtriranje "U šalici" iz teksta za daily reading
                fun filterCupReferences(text: String): String {
                    if (!isDailyReading) return text
                    return text
                        .replace(Regex("(?i)u ljubavi se vidi"), "Danas u ljubavi")
                        .replace(Regex("(?i)na poslu se vidi"), "Na poslu danas")
                        .replace(Regex("(?i)uz novac ide"), "S financijama danas")
                        .replace(Regex("(?i)za zdravlje stoji"), "Za zdravlje danas")
                        .replace(Regex("(?i)u šalici"), "danas")
                        .replace(Regex("(?i)šalici"), "danas")
                        .replace(Regex("(?i)talog"), "energija")
                        .replace(Regex("(?i)na dnu"), "danas")
                        .replace(Regex("(?i)uz rub"), "danas")
                        .replace(Regex("(?i)u sredini"), "danas")
                }
                
                if (isBapskiContent(result.love)) {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = tween(durationMillis = 600, delayMillis = getCardDelay(cardIndex++))) +
                                slideInVertically(
                                    initialOffsetY = { 30 },
                                    animationSpec = tween(durationMillis = 600, delayMillis = getCardDelay(cardIndex - 1))
                                )
                    ) {
                        ReadingCard(
                            title = "💕 Ljubav",
                            content = filterCupReferences(filterHoroscopeLanguage(result.love!!))
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                if (isBapskiContent(result.work)) {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = tween(durationMillis = 600, delayMillis = getCardDelay(cardIndex++))) +
                                slideInVertically(
                                    initialOffsetY = { 30 },
                                    animationSpec = tween(durationMillis = 600, delayMillis = getCardDelay(cardIndex - 1))
                                )
                    ) {
                        ReadingCard(
                            title = "💼 Posao",
                            content = filterCupReferences(filterHoroscopeLanguage(result.work!!))
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                if (isBapskiContent(result.money)) {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = tween(durationMillis = 600, delayMillis = getCardDelay(cardIndex++))) +
                                slideInVertically(
                                    initialOffsetY = { 30 },
                                    animationSpec = tween(durationMillis = 600, delayMillis = getCardDelay(cardIndex - 1))
                                )
                    ) {
                        ReadingCard(
                            title = "💰 Novac",
                            content = filterCupReferences(filterHoroscopeLanguage(result.money!!))
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                if (isBapskiContent(result.health)) {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = tween(durationMillis = 600, delayMillis = getCardDelay(cardIndex++))) +
                                slideInVertically(
                                    initialOffsetY = { 30 },
                                    animationSpec = tween(durationMillis = 600, delayMillis = getCardDelay(cardIndex - 1))
                                )
                    ) {
                        ReadingCard(
                            title = "🌿 Zdravlje",
                            content = filterCupReferences(filterHoroscopeLanguage(result.health!!))
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Symbols
                if (result.symbols.isNotEmpty()) {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = tween(durationMillis = 600, delayMillis = getCardDelay(cardIndex++))) +
                                slideInVertically(
                                    initialOffsetY = { 30 },
                                    animationSpec = tween(durationMillis = 600, delayMillis = getCardDelay(cardIndex - 1))
                                )
                    ) {
                        SymbolsCard(result.symbols)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Lucky Numbers
                if (result.luckyNumbers.isNotEmpty()) {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = tween(durationMillis = 600, delayMillis = getCardDelay(cardIndex++))) +
                                slideInVertically(
                                    initialOffsetY = { 30 },
                                    animationSpec = tween(durationMillis = 600, delayMillis = getCardDelay(cardIndex - 1))
                                )
                    ) {
                        LuckyNumbersCard(result.luckyNumbers)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // ⭐ HOROSKOPSKA POTVRDA (SEKUNDARNO - NA KRAJU, KAO POTVRDA)
                // Horoskop dolazi TEK NA KRAJU, kao potvrda onoga što je već viđeno u šalici
                // Koristi horoscopeMatch iz backend-a (ako postoji), inače fallback na generateHoroscopeConfirmation
                val horoscopeConfirmation = result.horoscopeMatch?.takeIf { it.isNotBlank() }
                    ?: zodiacSign?.let { 
                        generateHoroscopeConfirmation(it, result.visibleSymbols) 
                    }
                
                if (!horoscopeConfirmation.isNullOrBlank()) {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = tween(durationMillis = 600, delayMillis = getCardDelay(cardIndex++))) +
                                slideInVertically(
                                    initialOffsetY = { 30 },
                                    animationSpec = tween(durationMillis = 600, delayMillis = getCardDelay(cardIndex - 1))
                                )
                    ) {
                        ReadingCard(
                            title = "⭐ I zvijezde se slažu",
                            content = horoscopeConfirmation
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Error message
                if (saveError != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    com.gatalinka.app.ui.components.ErrorCard(
                        message = saveError ?: ErrorMessages.SAVE_FAILED,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
                
                // Save Button
                Button(
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        // Provjeri da li je placeholder (daily reading nema sliku)
                        val currentImageUri = if (imageUri == "daily_reading_placeholder" || imageUri.isEmpty()) {
                            "" // Prazan string za daily reading
                        } else {
                            imageUri.split("?")[0] // Ukloni query parametre ako postoje
                        }
                        coroutineScope.launch {
                            isSaving = true
                            saveError = null
                            try {
                                val cupReading = com.gatalinka.app.data.ReadingMapper.mapToCupReading(
                                    result,
                                    currentImageUri
                                ).copy(
                                    targetName = targetName,
                                    forSelf = targetName == null
                                )
                                readingsRepo.addReading(cupReading)
                                onSave()
                            } catch (e: Exception) {
                                saveError = when {
                                    e.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true ||
                                    e.message?.contains("permission", ignoreCase = true) == true ->
                                        ErrorMessages.PERMISSION_DENIED
                                    e.message?.contains("network", ignoreCase = true) == true ->
                                        ErrorMessages.NETWORK_ERROR
                                    else ->
                                        ErrorMessages.SAVE_FAILED
                                }
                                if (com.gatalinka.app.BuildConfig.DEBUG) {
                                    android.util.Log.e("ReadingResultScreen", "Error saving reading", e)
                                }
                            } finally {
                                isSaving = false
                            }
                        }
                    },
                    enabled = !isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GataUI.MysticGold,
                        contentColor = GataUI.MysticPurpleDeep,
                        disabledContainerColor = GataUI.MysticGold.copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = GataUI.MysticPurpleDeep,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            "Spremi čitanje",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            }
        }
    }
}

@Composable
fun GlassmorphismCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(com.gatalinka.app.ui.design.GataUI.CardCornerRadius)),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2D1B4E).copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(com.gatalinka.app.ui.design.GataUI.CardCornerRadius)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFFD700).copy(alpha = 0.1f),
                            Color(0xFF2D1B4E).copy(alpha = 0.2f)
                        )
                    )
                )
        ) {
            content()
        }
    }
}

@Composable
fun LuckScoreCard(score: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    GlassmorphismCard {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Sreća",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFD700) // Zlatna boja kao EnergyScoreCard
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "$score/100",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontSize = 48.sp, // Ista veličina kao EnergyScoreCard
                    fontWeight = FontWeight.Bold
                ),
                color = Color(0xFFFFD700) // Zlatna boja kao EnergyScoreCard
            )
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { score / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = Color(0xFFFFD700), // Zlatna boja kao EnergyScoreCard
                trackColor = Color(0xFFFFD700).copy(alpha = 0.3f) // Ista track boja kao EnergyScoreCard
            )
        }
    }
}

@Composable
fun ReadingCard(title: String, content: String) {
    GlassmorphismCard {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFD700),
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Text(
                content,
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFFEFE3D1),
                lineHeight = 24.sp,
                textAlign = TextAlign.Justify
            )
        }
    }
}

@Composable
fun VisibleSymbolsCard(
    symbols: List<com.gatalinka.app.ui.model.VisibleSymbol>,
    isDailyReading: Boolean = false
) {
    var isExpanded by remember { mutableStateOf(false) }
    val maxVisible = 7
    val shouldShowExpand = symbols.size > maxVisible
    val visibleSymbols = if (isExpanded) symbols else symbols.take(maxVisible)
    
    GlassmorphismCard {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                if (isDailyReading) "🔮 Simboli dana" else "🔮 Prepoznati simboli",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFD700),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            // Lista simbola s značenjem
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                visibleSymbols.forEach { visibleSymbol ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFFD700).copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = Color(0xFFFFD700).copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                Text(
                                    "✦",
                                    color = Color(0xFFFFD700),
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(
                                    visibleSymbol.symbol,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color(0xFFFFD700),
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                            if (visibleSymbol.meaning.isNotEmpty()) {
                                Text(
                                    visibleSymbol.meaning,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFFEFE3D1).copy(alpha = 0.9f),
                                    lineHeight = 20.sp,
                                    maxLines = 2,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
                
                // "Prikaži još" / "Prikaži manje" button
                if (shouldShowExpand) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (isExpanded) "Prikaži manje" else "Prikaži još (${symbols.size - maxVisible})",
                            color = Color(0xFFFFD700),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SymbolsCard(symbols: List<String>) {
    GlassmorphismCard {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                "🔮 Prepoznati simboli",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFD700),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            // Chips layout
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                symbols.forEach { symbol ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFFFD700).copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = Color(0xFFFFD700).copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "✦",
                                color = Color(0xFFFFD700),
                                modifier = Modifier.padding(end = 6.dp)
                            )
                            Text(
                                symbol,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFFFD700),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdviceCard(advice: String) {
    GlassmorphismCard {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Text(
                    "💡",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    "Savjet",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD700)
                )
            }
            Text(
                advice,
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFFEFE3D1),
                lineHeight = 24.sp,
                textAlign = TextAlign.Justify,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun LuckyNumbersCard(numbers: List<Int>) {
    GlassmorphismCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "🎲 Sretni brojevi",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFD700),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                numbers.forEach { number ->
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFFFD700),
                        modifier = Modifier
                            .size(60.dp)
                            .shadow(8.dp, CircleShape),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 2.dp,
                            color = Color(0xFF1A0B2E).copy(alpha = 0.3f)
                        )
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                number.toString(),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A0B2E)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EnergyScoreCard(score: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "energy_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    GlassmorphismCard {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Energija dana",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFD700)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "$score/100",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color(0xFFFFD700)
            )
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { score / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = Color(0xFFFFD700),
                trackColor = Color(0xFFFFD700).copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
fun MantraCard(mantra: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "mantra_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    GlassmorphismCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "✨ Dnevna mantra",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFD700),
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Text(
                mantra,
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFFEFE3D1),
                lineHeight = 24.sp,
                textAlign = TextAlign.Center,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }
    }
}
