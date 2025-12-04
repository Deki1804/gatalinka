package com.gatalinka.app.vm

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gatalinka.app.BuildConfig
import com.gatalinka.app.api.FirebaseFunctionsService
import com.gatalinka.app.data.Gender
import com.gatalinka.app.data.UserInput
import com.gatalinka.app.ui.model.GatalinkaReadingUiModel
import com.gatalinka.app.util.ZodiacCalculator
import kotlinx.coroutines.launch

class ReadingForOthersViewModel : ViewModel() {
    
    var customUserInput by mutableStateOf<UserInput?>(null)
    
    var personName by mutableStateOf("")
    
    fun updateName(name: String) {
        personName = name
    }
    
    fun updateBirthdate(dob: String) {
        val zodiac = ZodiacCalculator.calculateZodiac(dob)
        val current = customUserInput ?: UserInput()
        customUserInput = current.copy(
            birthdate = dob,
            zodiacSign = zodiac
        )
    }
    
    fun updateGender(g: Gender) {
        val current = customUserInput ?: UserInput()
        customUserInput = current.copy(gender = g)
    }
    
    fun clear() {
        customUserInput = null
        personName = ""
    }
    
    /**
     * Pokreće čitanje iz šalice koristeći viewModelScope da se ne prekine ako kompozicija napusti.
     */
    fun readCup(
        imageUri: Uri,
        context: Context,
        userInput: UserInput,
        readingMode: String,
        onSuccess: (GatalinkaReadingUiModel) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = FirebaseFunctionsService.readCup(
                    imageUri = imageUri,
                    context = context,
                    userInput = userInput,
                    readingMode = readingMode
                )
                
                if (!response.isValidCup) {
                    onError(com.gatalinka.app.util.ErrorMessages.getReadingErrorMessage(response.reason))
                    return@launch
                }
                
                if (response.safetyLevel == "nsfw" || response.reason == "nsfw_detected") {
                    onError(com.gatalinka.app.util.ErrorMessages.NSFW_DETECTED)
                    return@launch
                }
                
                // Mapiraj u UI model
                val uiModel = GatalinkaReadingUiModel(
                    mainText = response.mainText,
                    love = response.love,
                    work = response.work,
                    money = response.money,
                    health = response.health,
                    symbols = response.symbols,
                    luckyNumbers = response.luckyNumbers,
                    luckScore = response.luckScore,
                    mantra = response.mantra,
                    energyScore = response.energyScore
                )
                
                onSuccess(uiModel)
                
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) {
                    android.util.Log.e("ReadingForOthersViewModel", "Error reading cup", e)
                }
                onError(com.gatalinka.app.util.ErrorMessages.getErrorMessage(e))
            }
        }
    }
}


