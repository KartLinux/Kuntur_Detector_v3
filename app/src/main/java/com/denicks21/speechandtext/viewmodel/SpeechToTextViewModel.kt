package com.denicks21.speechandtext.viewmodel

import android.content.Context
import android.location.Location
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.denicks21.speechandtext.api.AlarmNotificationRequest
import com.denicks21.speechandtext.api.ApiService
import com.denicks21.speechandtext.api.ThreatAnalysisResponse
import com.denicks21.speechandtext.navigation.NavScreens
import com.denicks21.speechandtext.util.AudioVisualUtilities
import com.denicks21.speechandtext.util.KunturLogger
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class SpeechToTextViewModel : ViewModel() {
    private val apiService = ApiService()
    
    // Speech recognition states
    var speechInput by mutableStateOf("")
        private set
    
    // Operation mode
    var isManualMode by mutableStateOf(true)
        private set
    
    // Threat analysis
    private val _threatAnalysis = MutableStateFlow<ThreatAnalysisResponse?>(null)
    val threatAnalysis: StateFlow<ThreatAnalysisResponse?> = _threatAnalysis
    
    // Analysis status
    var isAnalyzing by mutableStateOf(false)
        private set
    
    // Alarm state
    var isAlarmActive by mutableStateOf(false)
        private set
    
    // Auto analysis job
    private var autoAnalysisJob: Job? = null
    
    // Audio Visual Utilities
    private var audioVisualUtils: AudioVisualUtilities? = null
    
    // Navigation controller reference (set from outside)
    var navController: NavController? = null
    
    fun initialize(context: Context) {
        KunturLogger.i("ViewModel initialized", "SPEECH_VIEWMODEL")
        audioVisualUtils = AudioVisualUtilities(context)
    }
    
    fun updateSpeechInput(text: String) {
        speechInput = text
        KunturLogger.logSpeechEvent("Speech input updated", text, "SPEECH_VIEWMODEL")
    }
    
    fun setOperationMode(isManual: Boolean) {
        val modeText = if (isManual) "Manual" else "Automatic"
        KunturLogger.logModeChange(modeText, "SPEECH_VIEWMODEL")
        
        isManualMode = isManual
        
        // Cancel any existing auto analysis job
        autoAnalysisJob?.cancel()
        autoAnalysisJob = null
        
        // Start automatic analysis if in auto mode
        if (!isManual) {
            KunturLogger.i("Starting automatic analysis mode", "SPEECH_VIEWMODEL")
            startAutoAnalysis()
        } else {
            KunturLogger.i("Switched to manual analysis mode", "SPEECH_VIEWMODEL")
        }
    }
    
    private fun startAutoAnalysis() {
        KunturLogger.i("Starting auto-analysis coroutine (20s intervals)", "SPEECH_VIEWMODEL")
        autoAnalysisJob = viewModelScope.launch {
            while (isActive) {
                delay(20_000) // 20 seconds
                
                if (!isManualMode && speechInput.isNotBlank()) {
                    KunturLogger.d("Auto-analysis triggered after 20s", "SPEECH_VIEWMODEL")
                    analyzeText()
                }
            }
        }
    }
    
    fun analyzeText() {
        if (speechInput.isBlank()) {
            KunturLogger.w("Analyze text called but speechInput is blank", "SPEECH_VIEWMODEL")
            return
        }
        
        KunturLogger.logUIAction("Analyze Text triggered", "SPEECH_VIEWMODEL")
        KunturLogger.logApiCall("Starting threat analysis", "PENDING", "SPEECH_VIEWMODEL")
        
        viewModelScope.launch {
            isAnalyzing = true
            
            try {
                val result = apiService.analyzeThreat(speechInput)
                _threatAnalysis.value = result
                
                KunturLogger.logApiCall("Threat analysis completed", "SUCCESS", "SPEECH_VIEWMODEL")
                KunturLogger.logThreatDetection(
                    result.is_threat == "SI", 
                    result.threat_type, 
                    "SPEECH_VIEWMODEL"
                )
                
                // Check if it's a threat
                if (result.is_threat == "SI") {
                    KunturLogger.w("THREAT DETECTED! Activating alarm system", "SPEECH_VIEWMODEL")
                    handleThreat(result)
                } else {
                    KunturLogger.i("No threat detected", "SPEECH_VIEWMODEL")
                }
                
            } catch (e: Exception) {
                // Handle API error
                KunturLogger.e("API call failed", "SPEECH_VIEWMODEL", e)
                KunturLogger.logApiCall("Threat analysis failed", "ERROR", "SPEECH_VIEWMODEL")
                e.printStackTrace()
            } finally {
                isAnalyzing = false
                KunturLogger.d("Analysis completed, isAnalyzing set to false", "SPEECH_VIEWMODEL")
            }
        }
    }
    
    private fun handleThreat(threatResult: ThreatAnalysisResponse) {
        KunturLogger.logAlarmEvent("Threat handler activated", "SPEECH_VIEWMODEL")
        viewModelScope.launch {
            isAlarmActive = true
            KunturLogger.logAlarmEvent("Alarm state set to active", "SPEECH_VIEWMODEL")
            
            // Start alarm sound and visual effects
            audioVisualUtils?.startAlarm()
            KunturLogger.logAlarmEvent("Audio/Visual alarm started", "SPEECH_VIEWMODEL")
            
            // Navigate to camera screen
            KunturLogger.logNavigationEvent("HomePage", "CameraScreen", "SPEECH_VIEWMODEL")
            navController?.navigate(NavScreens.CameraPreviewScreen.route)
        }
    }
    
    fun stopAlarm() {
        KunturLogger.logAlarmEvent("Stop alarm requested", "SPEECH_VIEWMODEL")
        viewModelScope.launch {
            isAlarmActive = false
            audioVisualUtils?.stopAlarm()
            KunturLogger.logAlarmEvent("Alarm stopped", "SPEECH_VIEWMODEL")
        }
    }
    
    fun sendAlarmNotification(location: String, videoUrl: String) {
        viewModelScope.launch {
            val threatData = _threatAnalysis.value ?: return@launch
            
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(Date())
            
            val alarmData = AlarmNotificationRequest(
                location = location,
                keyword = threatData.keyword,
                threat_type = threatData.threat_type,
                justification = threatData.justification,
                timestamp = timestamp,
                videoUrl = videoUrl
            )
            
            try {
                apiService.sendAlarmNotification(alarmData)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        audioVisualUtils?.stopAlarm()
        autoAnalysisJob?.cancel()
    }
    
    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SpeechToTextViewModel::class.java)) {
                return SpeechToTextViewModel() as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
