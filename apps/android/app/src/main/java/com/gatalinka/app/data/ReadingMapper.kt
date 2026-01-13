package com.gatalinka.app.data

import com.gatalinka.app.ui.model.GatalinkaReadingUiModel

/**
 * Helper funkcije za mapiranje između UI modela i data modela.
 */
object ReadingMapper {
    
    /**
     * Mapira GatalinkaReadingUiModel (iz API-ja) u CupReading (za storage).
     * Napomena: visibleSymbols, interpretation i advice se ne spremaju u CupReading
     * jer storage struktura nije promijenjena (prema zahtjevu).
     */
    fun mapToCupReading(
        uiModel: GatalinkaReadingUiModel,
        imageUri: String
    ): CupReading {
        return CupReading(
            id = java.util.UUID.randomUUID().toString(),
            imageUri = imageUri,
            timestamp = System.currentTimeMillis(),
            symbols = uiModel.symbols,
            interpretation = ReadingInterpretation(
                love = uiModel.love ?: "",
                career = uiModel.work ?: "",
                money = uiModel.money ?: "",
                health = uiModel.health ?: "",
                future = uiModel.mainText // Main text ide u future
            ),
            happinessScore = uiModel.luckScore,
            luckyNumbers = uiModel.luckyNumbers,
            advice = uiModel.advice ?: uiModel.mainText, // Koristi advice ako postoji, inače mainText
            zodiacContext = uiModel.interpretation ?: "", // Koristi interpretation ako postoji
            mantra = uiModel.mantra,
            energyScore = uiModel.energyScore
        )
    }
    
    /**
     * Mapira CupReading (iz storage-a) natrag u GatalinkaReadingUiModel (za prikaz).
     * Napomena: visibleSymbols se ne vraća jer nije spremljeno u storage.
     * Fallback: generiraj visibleSymbols iz symbols arraya za stara čitanja.
     */
    fun mapToUiModel(reading: CupReading): GatalinkaReadingUiModel {
        val mainText = reading.interpretation.future.ifEmpty { reading.advice }
        
        // Fallback za stara čitanja: generiraj visibleSymbols iz symbols arraya
        val visibleSymbols = if (!reading.symbols.isNullOrEmpty()) {
            reading.symbols.take(5).map { symbol ->
                com.gatalinka.app.ui.model.VisibleSymbol(
                    symbol = symbol,
                    meaning = generateGenericMeaning(symbol)
                )
            }
        } else {
            null
        }
        
        // Fallback za interpretation - koristi zodiacContext samo ako nije horoskop tekst
        val interpretation = reading.zodiacContext.takeIf { 
            it.isNotEmpty() && 
            it != mainText &&
            !it.contains("horoskop", ignoreCase = true) &&
            !it.contains("Ovan", ignoreCase = true) &&
            !it.contains("Lav", ignoreCase = true) &&
            !it.contains("blizanci", ignoreCase = true) &&
            !it.contains("rak", ignoreCase = true) &&
            !it.contains("vaga", ignoreCase = true) &&
            !it.contains("škorpion", ignoreCase = true) &&
            !it.contains("strijelac", ignoreCase = true) &&
            !it.contains("jarac", ignoreCase = true) &&
            !it.contains("vodenjak", ignoreCase = true) &&
            !it.contains("ribe", ignoreCase = true) &&
            !it.contains("bik", ignoreCase = true)
        }
        
        // Fallback za advice - koristi advice ako postoji i razlikuje se od mainText
        val advice = reading.advice.takeIf { 
            it.isNotEmpty() && it != reading.interpretation.future 
        } ?: generateDefaultAdvice()
        
        return GatalinkaReadingUiModel(
            mainText = mainText,
            visibleSymbols = visibleSymbols,
            interpretation = interpretation,
            advice = advice,
            love = reading.interpretation.love.takeIf { it.isNotEmpty() },
            work = reading.interpretation.career.takeIf { it.isNotEmpty() },
            money = reading.interpretation.money.takeIf { it.isNotEmpty() },
            health = reading.interpretation.health.takeIf { it.isNotEmpty() },
            symbols = reading.symbols,
            luckyNumbers = reading.luckyNumbers,
            luckScore = reading.happinessScore,
            mantra = reading.mantra,
            energyScore = reading.energyScore,
            horoscopeMatch = null // Stara čitanja nemaju horoscopeMatch
        )
    }
    
    private fun generateGenericMeaning(symbol: String): String {
        // Generičko značenje za stare simbole (fallback)
        return when {
            symbol.contains("ptica", ignoreCase = true) -> "Vijesti dolaze"
            symbol.contains("konj", ignoreCase = true) -> "Put i napredak"
            symbol.contains("pas", ignoreCase = true) -> "Prijatelj i vjernost"
            symbol.contains("mačka", ignoreCase = true) -> "Ljubomora ili oprez"
            symbol.contains("zmija", ignoreCase = true) -> "Ogovaranje ili izdaja"
            symbol.contains("riba", ignoreCase = true) -> "Novac ili dobitak"
            symbol.contains("put", ignoreCase = true) -> "Putovanje ili promjena"
            symbol.contains("kuća", ignoreCase = true) -> "Dom i obitelj"
            symbol.contains("krug", ignoreCase = true) -> "Brak ili zatvaranje kruga"
            symbol.contains("križ", ignoreCase = true) -> "Teret ili teškoća"
            symbol.contains("broj", ignoreCase = true) || symbol.any { it.isDigit() } -> "Vrijeme i važnost"
            else -> "Simbol u šalici"
        }
    }
    
    private fun generateDefaultAdvice(): String {
        val advices = listOf(
            "Ne brzaj, sve dolazi u svoje vrijeme.",
            "Pazi kome govoriš svoje planove.",
            "Vjeruj u sebe, ali i pazi na znakove.",
            "Dobro razmisli prije nego što doneseš važnu odluku.",
            "Ne zaboravi na one koji su ti uvijek bili uz tebe."
        )
        return advices.random()
    }
}

