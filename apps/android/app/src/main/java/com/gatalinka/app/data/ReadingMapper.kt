package com.gatalinka.app.data

import com.gatalinka.app.ui.model.GatalinkaReadingUiModel

/**
 * Helper funkcije za mapiranje između UI modela i data modela.
 */
object ReadingMapper {
    
    /**
     * Mapira GatalinkaReadingUiModel (iz API-ja) u CupReading (za storage).
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
            advice = uiModel.mainText,
            zodiacContext = "",
            mantra = uiModel.mantra,
            energyScore = uiModel.energyScore
        )
    }
    
    /**
     * Mapira CupReading (iz storage-a) natrag u GatalinkaReadingUiModel (za prikaz).
     */
    fun mapToUiModel(reading: CupReading): GatalinkaReadingUiModel {
        return GatalinkaReadingUiModel(
            mainText = reading.interpretation.future.ifEmpty { reading.advice },
            love = reading.interpretation.love.takeIf { it.isNotEmpty() },
            work = reading.interpretation.career.takeIf { it.isNotEmpty() },
            money = reading.interpretation.money.takeIf { it.isNotEmpty() },
            health = reading.interpretation.health.takeIf { it.isNotEmpty() },
            symbols = reading.symbols,
            luckyNumbers = reading.luckyNumbers,
            luckScore = reading.happinessScore,
            mantra = reading.mantra,
            energyScore = reading.energyScore
        )
    }
}

