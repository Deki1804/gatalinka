package com.gatalinka.app.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

object UserPreferencesKeys {
    val BIRTHDATE = stringPreferencesKey("birthdate")
    val GENDER = stringPreferencesKey("gender")
    val ZODIAC_SIGN = stringPreferencesKey("zodiac_sign")
    val HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")
    val DAILY_READING_CACHE = stringPreferencesKey("daily_reading_cache")
    val DAILY_READING_DATE = longPreferencesKey("daily_reading_date")
}

class UserPreferencesRepository(private val dataStore: DataStore<Preferences>) {
    
    val userInput: Flow<UserInput> = dataStore.data.map { preferences ->
        UserInput(
            birthdate = preferences[UserPreferencesKeys.BIRTHDATE] ?: "",
            gender = preferences[UserPreferencesKeys.GENDER]?.let { 
                try { Gender.valueOf(it) } catch (e: Exception) { Gender.Unspecified }
            } ?: Gender.Unspecified,
            zodiacSign = preferences[UserPreferencesKeys.ZODIAC_SIGN]?.let {
                try { com.gatalinka.app.util.ZodiacSign.valueOf(it) } catch (e: Exception) { null }
            }
        )
    }
    
    val hasCompletedOnboarding: Flow<Boolean> = dataStore.data.map { preferences ->
        val hasFlag = preferences[UserPreferencesKeys.HAS_COMPLETED_ONBOARDING] ?: false
        val hasBirthdate = !preferences[UserPreferencesKeys.BIRTHDATE].isNullOrEmpty()
        val hasGender = preferences[UserPreferencesKeys.GENDER] != null && 
                       preferences[UserPreferencesKeys.GENDER] != "Unspecified"
        // Onboarding je završen samo ako ima flag I ima datum I spol
        hasFlag && hasBirthdate && hasGender
    }
    
    suspend fun saveUserInput(input: UserInput) {
        dataStore.edit { preferences ->
            preferences[UserPreferencesKeys.BIRTHDATE] = input.birthdate
            preferences[UserPreferencesKeys.GENDER] = input.gender.name
            input.zodiacSign?.let {
                preferences[UserPreferencesKeys.ZODIAC_SIGN] = it.name
            }
            preferences[UserPreferencesKeys.HAS_COMPLETED_ONBOARDING] = true
        }
    }
    
    suspend fun clearOnboarding() {
        dataStore.edit { preferences ->
            preferences[UserPreferencesKeys.HAS_COMPLETED_ONBOARDING] = false
        }
    }
    
    private val gson = Gson()
    
    /**
     * Spremi daily reading u cache s datumom
     */
    suspend fun saveDailyReadingCache(reading: com.gatalinka.app.ui.model.GatalinkaReadingUiModel) {
        dataStore.edit { preferences ->
            val cache = DailyReadingCache.fromUiModel(reading)
            val json = gson.toJson(cache)
            preferences[UserPreferencesKeys.DAILY_READING_CACHE] = json
            preferences[UserPreferencesKeys.DAILY_READING_DATE] = System.currentTimeMillis()
        }
    }
    
    /**
     * Dohvati cached daily reading ako je za današnji dan
     */
    suspend fun getDailyReadingCache(): com.gatalinka.app.ui.model.GatalinkaReadingUiModel? {
        val preferences = dataStore.data.first()
        val cachedDate = preferences[UserPreferencesKeys.DAILY_READING_DATE] ?: 0L
        val cachedJson = preferences[UserPreferencesKeys.DAILY_READING_CACHE] ?: return null
        
        // Provjeri je li cache za današnji dan
        val today = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis
        
        if (cachedDate >= today) {
            try {
                val cache = gson.fromJson(cachedJson, DailyReadingCache::class.java)
                return cache.toUiModel()
            } catch (e: Exception) {
                android.util.Log.w("UserPreferencesRepository", "Failed to parse daily reading cache", e)
                return null
            }
        }
        return null
    }
}

private data class DailyReadingCache(
    @SerializedName("mainText") val mainText: String,
    @SerializedName("love") val love: String,
    @SerializedName("work") val work: String,
    @SerializedName("money") val money: String,
    @SerializedName("health") val health: String,
    @SerializedName("symbols") val symbols: List<String>,
    @SerializedName("luckyNumbers") val luckyNumbers: List<Int>,
    @SerializedName("luckScore") val luckScore: Int,
    @SerializedName("mantra") val mantra: String,
    @SerializedName("energyScore") val energyScore: Int
) {
    companion object {
        fun fromUiModel(model: com.gatalinka.app.ui.model.GatalinkaReadingUiModel): DailyReadingCache {
            return DailyReadingCache(
                mainText = model.mainText,
                love = model.love ?: "",
                work = model.work ?: "",
                money = model.money ?: "",
                health = model.health ?: "",
                symbols = model.symbols,
                luckyNumbers = model.luckyNumbers,
                luckScore = model.luckScore,
                mantra = model.mantra,
                energyScore = model.energyScore
            )
        }
    }
    
    fun toUiModel(): com.gatalinka.app.ui.model.GatalinkaReadingUiModel {
        return com.gatalinka.app.ui.model.GatalinkaReadingUiModel(
            mainText = mainText,
            love = love.takeIf { it.isNotEmpty() },
            work = work.takeIf { it.isNotEmpty() },
            money = money.takeIf { it.isNotEmpty() },
            health = health.takeIf { it.isNotEmpty() },
            symbols = symbols,
            luckyNumbers = luckyNumbers,
            luckScore = luckScore,
            mantra = mantra,
            energyScore = energyScore
        )
    }
}





