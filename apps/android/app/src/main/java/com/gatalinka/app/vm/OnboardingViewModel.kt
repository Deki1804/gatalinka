package com.gatalinka.app.vm

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.gatalinka.app.data.CupReading
import com.gatalinka.app.data.Gender
import com.gatalinka.app.data.UserInput
import com.gatalinka.app.util.ZodiacCalculator
import com.gatalinka.app.util.ZodiacSign

class OnboardingViewModel : ViewModel() {

    var state by mutableStateOf(UserInput())
        private set

    var readings by mutableStateOf<List<CupReading>>(emptyList())
        private set

    fun updateBirthdate(dob: String) {
        val zodiac = ZodiacCalculator.calculateZodiac(dob)
        state = state.copy(birthdate = dob, zodiacSign = zodiac)
    }

    fun updateGender(g: Gender) {
        state = state.copy(gender = g)
    }

    fun acceptDisclaimer() {
        state = state.copy(acceptedDisclaimer = true)
    }

    fun addReading(reading: CupReading) {
        readings = readings + reading
    }

    fun getReadingsSorted(): List<CupReading> {
        return readings.sortedByDescending { it.timestamp }
    }
}
