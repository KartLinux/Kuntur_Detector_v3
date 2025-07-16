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
        audioVisualUtils = AudioVisualUtilities(context)
    }
    
    fun updateSpeechInput(text: String) {
        speechInput = text
    }
    
    fun setOperationMode(isManual: Boolean) {
        isManualMode = isManual
        
        // Cancel any existing auto analysis job
        autoAnalysisJob?.cancel()
        autoAnalysisJob = null
        
        // Start automatic analysis if in auto mode
        if (!isManual) {
            startAutoAnalysis()
        }
    }
    
    private fun startAutoAnalysis() {
        autoAnalysisJob = viewModelScope.launch {
            while (isActive) {
                delay(20_000) // 20 seconds
                
                if (!isManualMode && speechInput.isNotBlank()) {
                    analyzeText()
                }
            }
        }
    }
    
    fun analyzeText() {
        if (speechInput.isBlank()) return
        
        viewModelScope.launch {
            isAnalyzing = true
            
            try {
                val result = apiService.analyzeThreat(speechInput)
                _threatAnalysis.value = result
                
                // Check if it's a threat
                if (result.is_threat == "SI") {
                    handleThreat(result)
                }
                
            } catch (e: Exception) {
                // Handle API error
                e.printStackTrace()
            } finally {
                isAnalyzing = false
            }
        }
    }
    
    private fun handleThreat(threatResult: ThreatAnalysisResponse) {
        viewModelScope.launch {
            isAlarmActive = true
            
            // Start alarm sound and visual effects
            audioVisualUtils?.startAlarm()
            
            // Navigate to camera screen
            navController?.navigate(NavScreens.CameraPreviewScreen.route)
        }
    }
    
    fun stopAlarm() {
        viewModelScope.launch {
            isAlarmActive = false
            audioVisualUtils?.stopAlarm()
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
