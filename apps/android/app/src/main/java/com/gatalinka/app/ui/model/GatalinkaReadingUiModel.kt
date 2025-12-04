package com.gatalinka.app.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * UI model za prikaz rezultata čitanja.
 */
@Parcelize
data class GatalinkaReadingUiModel(
    val mainText: String,
    val love: String?,
    val work: String?,
    val money: String?,
    val health: String?,
    val symbols: List<String>,
    val luckyNumbers: List<Int>,
    val luckScore: Int, // 0–100
    val mantra: String, // Dnevna mantra/poruka
    val energyScore: Int // 0–100, opća energija dana
) : Parcelable



