package com.denicks21.speechandtext.api

data class ThreatAnalysisRequest(
    val text: String
)

data class ThreatAnalysisResponse(
    val keyword: String,
    val threat_type: String,
    val is_threat: String, // "SI" or "NO"
    val justification: String
)

data class AlarmNotificationRequest(
    val location: String,
    val keyword: String,
    val threat_type: String,
    val justification: String,
    val timestamp: String,
    val videoUrl: String
)
// en algún archivo común como models/VideoFile.kt
data class VideoFile(
    val uri: String,
    val name: String
)
data class UserLocation(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val address: String = "Obteniendo ubicación..."
)
