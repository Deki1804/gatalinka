package com.gatalinka.app.data

import com.gatalinka.app.util.ZodiacSign

data class UserInput(
    val birthdate: String = "",
    val gender: Gender = Gender.Unspecified,
    val zodiacSign: ZodiacSign? = null,
    val acceptedDisclaimer: Boolean = false
)

enum class Gender { Male, Female, Other, Unspecified }

data class CupReading(
    val id: String = java.util.UUID.randomUUID().toString(),
    val imageUri: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val symbols: List<String> = emptyList(),
    val interpretation: ReadingInterpretation = ReadingInterpretation(),
    val happinessScore: Int = 0,
    val luckyNumbers: List<Int> = emptyList(),
    val advice: String = "",
    val zodiacContext: String = "",
    val mantra: String = "", // Dnevna mantra/poruka
    val energyScore: Int = 50, // 0–100, opća energija dana
    val targetName: String? = null, // Ime osobe za koju je gatanje (null = za sebe)
    val forSelf: Boolean = true // true = za sebe, false = za drugu osobu
)

data class ReadingInterpretation(
    val love: String = "",
    val career: String = "",
    val money: String = "",
    val health: String = "",
    val future: String = ""
)
