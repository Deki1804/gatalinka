package com.gatalinka.app.core.audio

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import com.gatalinka.app.BuildConfig

class AudioEngine(private val context: Context) {
    private var backgroundPlayer: MediaPlayer? = null
    private var sfxPlayer: MediaPlayer? = null

    fun playBackgroundMusic(resId: Int) {
        try {
            stopBackgroundMusic()
            backgroundPlayer = MediaPlayer.create(context, resId).apply {
                isLooping = true
                setVolume(0.3f, 0.3f) // Tiša pozadina
                start()
            }
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) {
                Log.e("AudioEngine", "Error playing background music", e)
            }
        }
    }

    fun stopBackgroundMusic() {
        try {
            backgroundPlayer?.stop()
            backgroundPlayer?.release()
            backgroundPlayer = null
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) {
                Log.e("AudioEngine", "Error stopping background music", e)
            }
        }
    }

    fun playSfx(resId: Int) {
        try {
            sfxPlayer?.release()
            sfxPlayer = MediaPlayer.create(context, resId).apply {
                setVolume(1.0f, 1.0f)
                start()
                setOnCompletionListener {
                    it.release()
                }
            }
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) {
                Log.e("AudioEngine", "Error playing SFX", e)
            }
        }
    }
}
