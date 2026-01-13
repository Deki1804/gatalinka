package com.gatalinka.app.ui.screens

import android.content.ClipData
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract

/**
 * Custom ActivityResultContract za single image selection s eksplicitnim flagovima
 * koji osiguravaju da se ne pamti prethodni odabir.
 */
class SingleImagePickerContract : ActivityResultContract<String, Uri?>() {
    override fun createIntent(context: android.content.Context, input: String): Intent {
        return Intent(Intent.ACTION_GET_CONTENT).apply {
            type = input
            // Eksplicitno postavi single selection
            addCategory(Intent.CATEGORY_OPENABLE)
            // VAŽNO: Eksplicitno zabrani višestruki odabir
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
            // Dodaj dodatne flagove da se osigura da se ne pamti prethodni odabir
            // FLAG_ACTIVITY_CLEAR_TOP osigurava da se ne koristi postojeći task
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            // Eksplicitno postavi da se ne koristi višestruki odabir
            putExtra("android.intent.extra.ALLOW_MULTIPLE", false)
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        if (intent == null || resultCode != android.app.Activity.RESULT_OK) {
            return null
        }
        
        // Osiguraj da se uzme samo prvi URI (single selection)
        val clipData: ClipData? = intent.clipData
        return when {
            clipData != null && clipData.itemCount > 0 -> {
                // Ako postoji ClipData, uzmi samo prvi item
                clipData.getItemAt(0).uri
            }
            intent.data != null -> {
                // Ako postoji direktni data URI
                intent.data
            }
            else -> null
        }
    }
}

