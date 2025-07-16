package com.denicks21.speechandtext.util

import android.content.Context
import android.hardware.camera2.CameraManager
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.content.ContextCompat
import com.denicks21.speechandtext.R
import com.google.common.collect.ComparisonChain.start
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class AudioVisualUtilities(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var cameraManager: CameraManager? = null
    private var cameraId: String? = null
    private var isFlashlightOn = false
    private var flashlightTimer: Timer? = null
    private var isPlaying = false
    
    init {
        setupVibrator()
        setupFlashlight()
    }
    
    private fun setupVibrator() {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }
    
    private fun setupFlashlight() {
        cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            cameraId = cameraManager?.cameraIdList?.firstOrNull()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    suspend fun startAlarm() = withContext(Dispatchers.Main) {
        if (isPlaying) return@withContext
        isPlaying = true
        
        // Start siren sound
        mediaPlayer = MediaPlayer.create(context, R.raw.tobeloved).apply {
            start()
        }
        
        // Start vibration pattern
        startVibration()
        
        // Start flashlight blinking
        startFlashlight()
    }
    
    fun stopAlarm() {
        isPlaying = false
        
        // Stop sound
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null
        
        // Stop vibration
        vibrator?.cancel()
        
        // Stop flashlight
        stopFlashlight()
    }
    
    private fun startVibration() {
        val pattern = longArrayOf(0, 500, 500, 500, 500, 500, 500) // on/off pattern
        val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255, 0)     // vibration strength
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, 0))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, 0)
        }
    }
    
    private fun startFlashlight() {
        flashlightTimer = Timer().apply {
            schedule(object : TimerTask() {
                override fun run() {
                    if (!isPlaying) {
                        cancel()
                        return
                    }
                    
                    try {
                        if (cameraId != null) {
                            cameraManager?.setTorchMode(cameraId!!, !isFlashlightOn)
                            isFlashlightOn = !isFlashlightOn
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }, 0, 500) // Toggle every 500ms
        }
    }
    
    private fun stopFlashlight() {
        flashlightTimer?.cancel()
        flashlightTimer = null
        
        try {
            if (isFlashlightOn && cameraId != null) {
                cameraManager?.setTorchMode(cameraId!!, false)
                isFlashlightOn = false
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
