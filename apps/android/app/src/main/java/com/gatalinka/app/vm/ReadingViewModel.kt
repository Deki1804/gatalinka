package com.gatalinka.app.vm

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gatalinka.app.api.FirebaseFunctionsService
import com.gatalinka.app.api.dto.GatalinkaReadingDto
import com.gatalinka.app.data.UserInput
import com.gatalinka.app.ui.model.GatalinkaReadingUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.ensureActive

/**
 * ViewModel za upravljanje procesom čitanja iz kave.
 */
class ReadingViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow<ReadingUiState>(ReadingUiState.Loading)
    val uiState: StateFlow<ReadingUiState> = _uiState.asStateFlow()
    
    /**
     * Pokreće proces čitanja iz kave.
     * 
     * @param imageUri URI slike šalice
     * @param context Application context za čitanje slike
     * @param userInput Korisnički podaci (zodiac, gender) za personalizaciju
     */
    fun startReading(imageUri: Uri, context: Context, userInput: UserInput? = null) {
        viewModelScope.launch {
            try {
                _uiState.value = ReadingUiState.Loading
                
                val response = FirebaseFunctionsService.readCup(
                    imageUri = imageUri,
                    context = context,
                    userInput = userInput
                )
                
                ensureActive()
                
                if (!response.isValidCup) {
                    _uiState.value = ReadingUiState.Error(
                        type = ErrorType.NOT_A_CUP,
                        message = mapReasonToMessage(response.reason)
                    )
                    return@launch
                }
                
                if (response.safetyLevel == "nsfw" || response.reason == "nsfw_detected") {
                    _uiState.value = ReadingUiState.Error(
                        type = ErrorType.NSFW,
                        message = mapReasonToMessage("nsfw_detected")
                    )
                    return@launch
                }
                
                ensureActive()
                val uiModel = mapDtoToUiModel(response)
                ensureActive()
                
                _uiState.value = ReadingUiState.Success(uiModel)
                if (com.gatalinka.app.BuildConfig.DEBUG) {
                    Log.d("ReadingViewModel", "✅ State set to Success: luckScore=${uiModel.luckScore}")
                }
                
            } catch (e: kotlinx.coroutines.CancellationException) {
                if (com.gatalinka.app.BuildConfig.DEBUG) {
                    Log.w("ReadingViewModel", "Cancelled", e)
                }
                throw e
            } catch (e: Exception) {
                if (com.gatalinka.app.BuildConfig.DEBUG) {
                    Log.e("ReadingViewModel", "Error", e)
                }
                _uiState.value = ReadingUiState.Error(
                    type = ErrorType.NETWORK_ERROR,
                    message = getErrorMessage(e)
                )
            }
        }
    }
    
    /**
     * Pokušava ponovno čitanje.
     */
    fun retry(imageUri: Uri, context: Context, userInput: UserInput? = null) {
        startReading(imageUri, context, userInput)
    }
    
    /**
     * Resetira state (korisno za novi flow).
     */
    fun reset() {
        _uiState.value = ReadingUiState.Loading
    }
    
    
    private fun mapDtoToUiModel(dto: GatalinkaReadingDto): GatalinkaReadingUiModel {
        return GatalinkaReadingUiModel(
            mainText = dto.mainText,
            love = dto.love,
            work = dto.work,
            money = dto.money,
            health = dto.health,
            symbols = dto.symbols,
            luckyNumbers = dto.luckyNumbers,
            luckScore = dto.luckScore,
            mantra = dto.mantra,
            energyScore = dto.energyScore
        )
    }
    
    private fun mapReasonToMessage(reason: String): String {
        return when (reason) {
            "image_too_small", "image_too_small_dimensions" ->
                "Fotografija je premala ili preniske kvalitete. Pokušaj približiti šalicu i slikati u boljem svjetlu."
            
            "bad_aspect_ratio" ->
                "Šalicu treba slikati odozgo, tako da bude unutar kružnog okvira. Pokušaj ponovo s pogleda odozgo."
            
            "too_dark" ->
                "Fotografija je previše tamna. Uključi svjetlo ili priđi bliže prozoru pa pokušaj ponovno."
            
            "too_bright" ->
                "Fotografija je previše svijetla. Pokušaj bez blica ili malo dalje od svjetla."
            
            "low_contrast" ->
                "Fotografija je mutna ili bez dovoljno detalja. Pokušaj ponovno, drži mobitel mirnije."
            
            "analysis_failed" ->
                "Nešto je pošlo po zlu prilikom analize slike. Pokušaj ponovno ili odaberi drugu fotografiju."
            
            "nsfw_detected" ->
                "Fotografija nije prikladna za čitanje šalice."
            
            else ->
                "Ova fotografija vjerojatno nije dobra za čitanje šalice. Pokušaj ponovno uz jasnu sliku šalice odozgo."
        }
    }
    
    private fun getErrorMessage(e: Exception): String {
        // Proveri da li je greška vezana za autentifikaciju
        return when {
            (e.message?.contains("prijavljen", ignoreCase = true) == true) ||
            (e.message?.contains("authenticated", ignoreCase = true) == true) ||
            (e.message?.contains("UNAUTHENTICATED", ignoreCase = true) == true) ->
                "Niste prijavljeni. Molimo prijavite se i pokušajte ponovo."
            (e.message?.contains("timeout", ignoreCase = true) == true) ->
                "Vrijeme čekanja je isteklo. Pokušaj ponovo."
            (e.message?.contains("network", ignoreCase = true) == true) ->
                "Ne mogu se spojiti na Gatalinku. Provjeri internetsku vezu."
            (e.message?.contains("Unable to resolve host", ignoreCase = true) == true) ->
                "Ne mogu se spojiti na server. Provjeri internetsku vezu."
            else ->
                "Nešto je pošlo po zlu: ${e.message ?: e.javaClass.simpleName}"
        }
    }
}

/**
 * Tip greške za čitanje.
 */
enum class ErrorType {
    NETWORK_ERROR,
    NOT_A_CUP,
    NSFW,
    UNKNOWN
}

/**
 * UI state za čitanje.
 */
sealed class ReadingUiState {
    data object Loading : ReadingUiState()
    data class Error(
        val type: ErrorType,
        val message: String
    ) : ReadingUiState()
    data class Success(
        val result: GatalinkaReadingUiModel
    ) : ReadingUiState()
    
    companion object {
        fun initial(): ReadingUiState = Loading
    }
}


