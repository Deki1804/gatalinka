package com.gatalinka.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.readingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "readings")

object ReadingsKeys {
    val READINGS_LIST = stringPreferencesKey("readings_list")
}

class ReadingsRepository(private val context: Context) {
    
    private val gson = Gson()
    
    val readings: Flow<List<CupReading>> = context.readingsDataStore.data.map { preferences ->
        val readingsJson = preferences[ReadingsKeys.READINGS_LIST] ?: "[]"
        try {
            val type = object : TypeToken<List<CupReading>>() {}.type
            gson.fromJson<List<CupReading>>(readingsJson, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun addReading(reading: CupReading) {
        context.readingsDataStore.edit { preferences ->
            val currentJson = preferences[ReadingsKeys.READINGS_LIST] ?: "[]"
            val type = object : TypeToken<List<CupReading>>() {}.type
            val currentReadings = try {
                gson.fromJson<List<CupReading>>(currentJson, type) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
            
            val updatedReadings = currentReadings + reading
            preferences[ReadingsKeys.READINGS_LIST] = gson.toJson(updatedReadings)
        }
    }
    
    suspend fun deleteReading(readingId: String) {
        context.readingsDataStore.edit { preferences ->
            val currentJson = preferences[ReadingsKeys.READINGS_LIST] ?: "[]"
            val type = object : TypeToken<List<CupReading>>() {}.type
            val currentReadings = try {
                gson.fromJson<List<CupReading>>(currentJson, type) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
            
            val updatedReadings = currentReadings.filter { it.id != readingId }
            preferences[ReadingsKeys.READINGS_LIST] = gson.toJson(updatedReadings)
        }
    }
    
    suspend fun getReadingById(readingId: String): CupReading? {
        val currentJson = context.readingsDataStore.data.map { 
            it[ReadingsKeys.READINGS_LIST] ?: "[]" 
        }.first()
        
        val type = object : TypeToken<List<CupReading>>() {}.type
        val readings = try {
            gson.fromJson<List<CupReading>>(currentJson, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
        
        return readings.find { it.id == readingId }
    }
    
    fun getReadingsSorted(): Flow<List<CupReading>> {
        return readings.map { it.sortedByDescending { reading -> reading.timestamp } }
    }
}

