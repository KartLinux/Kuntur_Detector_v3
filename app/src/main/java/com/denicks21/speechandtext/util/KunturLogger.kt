package com.denicks21.speechandtext.util

import android.util.Log

/**
 * Utility class for centralized logging with a unique tag for the Kuntur project
 */
object KunturLogger {
    private const val TAG = "KUNTUR_FLOW"
    
    // Niveles de log
    fun d(message: String, component: String = "") {
        Log.d(TAG, "[$component] $message")
    }
    
    fun i(message: String, component: String = "") {
        Log.i(TAG, "[$component] $message")
    }
    
    fun w(message: String, component: String = "") {
        Log.w(TAG, "[$component] $message")
    }
    
    fun e(message: String, component: String = "", throwable: Throwable? = null) {
        if (throwable != null) {
            Log.e(TAG, "[$component] $message", throwable)
        } else {
            Log.e(TAG, "[$component] $message")
        }
    }
    
    // Métodos específicos para eventos del flujo
    fun logUIAction(action: String, component: String = "UI") {
        i("UI_ACTION: $action", component)
    }
    
    fun logModeChange(mode: String, component: String = "MODE") {
        i("MODE_CHANGE: Changed to $mode", component)
    }
    
    fun logSpeechEvent(event: String, text: String = "", component: String = "SPEECH") {
        i("SPEECH_EVENT: $event - Text: ${text.take(50)}${if(text.length > 50) "..." else ""}", component)
    }
    
    fun logApiCall(endpoint: String, status: String, component: String = "API") {
        i("API_CALL: $endpoint - Status: $status", component)
    }
    
    fun logThreatDetection(isThread: Boolean, threatType: String, component: String = "THREAT") {
        w("THREAT_DETECTION: Threat=${isThread}, Type=$threatType", component)
    }
    
    fun logCameraEvent(event: String, component: String = "CAMERA") {
        i("CAMERA_EVENT: $event", component)
    }
    
    fun logAlarmEvent(event: String, component: String = "ALARM") {
        w("ALARM_EVENT: $event", component)
    }
    
    fun logNavigationEvent(from: String, to: String, component: String = "NAVIGATION") {
        i("NAVIGATION: $from -> $to", component)
    }

    fun logError(s: String, s1: String) {
        e("NAVIGATION: $s -> $", s1)
    }

    fun logInfo(s: String, s1: String) {
        i("NAVIGATION: $s -> $", s1)
    }

    fun logWarning(s: String, s1: String) {
        w("NAVIGATION: $s -> $", s1)
    }

    /**
     * Método general de log que imprime el mensaje como información
     * @param message El mensaje a registrar
     * @param component El componente que genera el log (usado como etiqueta secundaria)
     */
    fun log(message: String, component: String) {
        Log.d(component, message)
        Log.d(TAG, "[$component] $message")
    }
}
