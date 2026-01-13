package com.gatalinka.app.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * UI model za simbol s značenjem (bapsko gatanje).
 */
@Parcelize
data class VisibleSymbol(
    val symbol: String,
    val meaning: String
) : Parcelable

/**
 * UI model za prikaz rezultata čitanja (bapski stil).
 */
@Parcelize
data class GatalinkaReadingUiModel(
    val mainText: String,
    val visibleSymbols: List<VisibleSymbol>? = null, // Lista simbola s značenjem
    val interpretation: String? = null, // Kako se to tumači (bapski stil)
    val advice: String? = null, // Kratki savjet (1-2 rečenice)
    val love: String?,
    val work: String?,
    val money: String?,
    val health: String?,
    val symbols: List<String>, // Lista imena simbola (za kompatibilnost)
    val luckyNumbers: List<Int>,
    val luckScore: Int, // 0–100
    val mantra: String, // Dnevna mantra/poruka
    val energyScore: Int, // 0–100, opća energija dana
    val horoscopeMatch: String? = null // Kratka horoskopska potvrda iz backend-a (1-2 rečenice, opcionalno)
) : Parcelable



