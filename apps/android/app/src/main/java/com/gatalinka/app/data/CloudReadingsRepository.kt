package com.gatalinka.app.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date

/**
 * Cloud repository za spremanje čitanja u Firebase Firestore.
 * Sve čitanja se spremaju direktno u cloud i sinhroniziraju s drugim uređajima.
 */
class CloudReadingsRepository {
    
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    /**
     * Collection path za korisnička čitanja.
     * Format: users/{userId}/readings/{readingId}
     */
    private fun getReadingsCollection(): String {
        val userId = auth.currentUser?.uid
            ?: throw IllegalStateException("Korisnik nije prijavljen")
        return "users/$userId/readings"
    }
    
    /**
     * Flow koji emitira listu čitanja iz cloud-a.
     * Automatski se ažurira kada se dodaju nova čitanja.
     */
    val readings: Flow<List<CupReading>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        
        val collectionRef = firestore.collection("users/$userId/readings")
            .orderBy("timestamp", Query.Direction.DESCENDING)
        
        val listenerRegistration = collectionRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                // Ako ima greške, šalji praznu listu
                trySend(emptyList())
                return@addSnapshotListener
            }
            
            if (snapshot != null) {
                val readingsList = snapshot.documents.mapNotNull { document ->
                    try {
                        documentToCupReading(document.id, document.data ?: emptyMap())
                    } catch (e: Exception) {
                        null // Preskoči neispravna čitanja
                    }
                }
                trySend(readingsList)
            } else {
                trySend(emptyList())
            }
        }
        
        awaitClose { listenerRegistration.remove() }
    }
    
    /**
     * Dodaje novo čitanje u cloud.
     */
    suspend fun addReading(reading: CupReading) {
        val userId = auth.currentUser?.uid
            ?: throw IllegalStateException("Korisnik nije prijavljen")
        
        val readingData = cupReadingToMap(reading)
        
        firestore.collection("users/$userId/readings")
            .document(reading.id)
            .set(readingData)
            .await()
    }
    
    /**
     * Briše čitanje iz cloud-a.
     */
    suspend fun deleteReading(readingId: String) {
        val userId = auth.currentUser?.uid
            ?: throw IllegalStateException("Korisnik nije prijavljen")
        
        firestore.collection("users/$userId/readings")
            .document(readingId)
            .delete()
            .await()
    }
    
    /**
     * Dohvaća čitanje po ID-u.
     */
    suspend fun getReadingById(readingId: String): CupReading? {
        val userId = auth.currentUser?.uid
            ?: return null
        
        val document = firestore.collection("users/$userId/readings")
            .document(readingId)
            .get()
            .await()
        
        if (document.exists()) {
            return documentToCupReading(readingId, document.data ?: emptyMap())
        }
        
        return null
    }
    
    /**
     * Vraća sortiranu listu čitanja (najnovija prvo).
     */
    fun getReadingsSorted(): Flow<List<CupReading>> {
        return readings // Već su sortirana u query-ju
    }
    
    /**
     * Konvertira CupReading u Map za Firestore.
     */
    private fun cupReadingToMap(reading: CupReading): Map<String, Any> {
        return mapOf(
            "imageUri" to reading.imageUri,
            "timestamp" to reading.timestamp,
            "symbols" to reading.symbols,
            "interpretation" to mapOf(
                "love" to reading.interpretation.love,
                "career" to reading.interpretation.career,
                "money" to reading.interpretation.money,
                "health" to reading.interpretation.health,
                "future" to reading.interpretation.future
            ),
            "happinessScore" to reading.happinessScore,
            "luckyNumbers" to reading.luckyNumbers,
            "advice" to reading.advice,
            "zodiacContext" to reading.zodiacContext,
            "mantra" to reading.mantra,
            "energyScore" to reading.energyScore,
            "targetName" to (reading.targetName ?: ""),
            "forSelf" to reading.forSelf
        )
    }
    
    /**
     * Konvertira Firestore dokument u CupReading.
     */
    private fun documentToCupReading(id: String, data: Map<String, Any>): CupReading {
        val interpretation = (data["interpretation"] as? Map<*, *>) ?: emptyMap<String, Any>()
        
        val targetNameStr = data["targetName"] as? String
        return CupReading(
            id = id,
            imageUri = data["imageUri"] as? String ?: "",
            timestamp = (data["timestamp"] as? Long) ?: System.currentTimeMillis(),
            symbols = (data["symbols"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
            interpretation = ReadingInterpretation(
                love = interpretation["love"] as? String ?: "",
                career = interpretation["career"] as? String ?: "",
                money = interpretation["money"] as? String ?: "",
                health = interpretation["health"] as? String ?: "",
                future = interpretation["future"] as? String ?: ""
            ),
            happinessScore = (data["happinessScore"] as? Long)?.toInt() ?: (data["happinessScore"] as? Int) ?: 0,
            luckyNumbers = (data["luckyNumbers"] as? List<*>)?.mapNotNull { 
                when (it) {
                    is Long -> it.toInt()
                    is Int -> it
                    else -> null
                }
            } ?: emptyList(),
            advice = data["advice"] as? String ?: "",
            zodiacContext = data["zodiacContext"] as? String ?: "",
            mantra = data["mantra"] as? String ?: "",
            energyScore = (data["energyScore"] as? Long)?.toInt() ?: (data["energyScore"] as? Int) ?: 50,
            targetName = targetNameStr?.takeIf { it.isNotEmpty() },
            forSelf = (data["forSelf"] as? Boolean) ?: (targetNameStr == null || targetNameStr.isEmpty())
        )
    }
}

