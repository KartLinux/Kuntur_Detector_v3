package com.denicks21.speechandtext.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class ApiService {
    companion object {
        private const val API_BASE_URL = "http://192.168.1.70:8000"
        private const val ANALYSIS_ENDPOINT = "/analysis"
        private const val ALARM_ENDPOINT = "/alarm"
    }

    suspend fun analyzeThreat(text: String): ThreatAnalysisResponse = withContext(Dispatchers.IO) {
        val url = URL("$API_BASE_URL$ANALYSIS_ENDPOINT")
        val connection = url.openConnection() as HttpURLConnection
        
        try {
            // Configure connection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Accept", "application/json")
            connection.doOutput = true
            
            // Create request JSON
            val requestJson = JSONObject().apply {
                put("text", text)
            }
            
            // Send request
            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(requestJson.toString())
                writer.flush()
            }
            
            // Parse response
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                    val stringBuilder = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        stringBuilder.append(line)
                    }
                    stringBuilder.toString()
                }
                
                val jsonObject = JSONObject(response)
                return@withContext ThreatAnalysisResponse(
                    keyword = jsonObject.getString("keyword"),
                    threat_type = jsonObject.getString("threat_type"),
                    is_threat = jsonObject.getString("is_threat"),
                    justification = jsonObject.getString("justification")
                )
            } else {
                throw Exception("API call failed with response code: $responseCode")
            }
        } finally {
            connection.disconnect()
        }
    }

    suspend fun sendAlarmNotification(alarm: AlarmNotificationRequest): Boolean = withContext(Dispatchers.IO) {
        val url = URL("$API_BASE_URL$ALARM_ENDPOINT")
        val connection = url.openConnection() as HttpURLConnection
        
        try {
            // Configure connection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Accept", "application/json")
            connection.doOutput = true
            
            // Create request JSON
            val requestJson = JSONObject().apply {
                put("location", alarm.location)
                put("keyword", alarm.keyword)
                put("threat_type", alarm.threat_type)
                put("justification", alarm.justification)
                put("timestamp", alarm.timestamp)
                put("videoUrl", alarm.videoUrl)
            }
            
            // Send request
            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(requestJson.toString())
                writer.flush()
            }
            
            // Check response
            val responseCode = connection.responseCode
            return@withContext (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED)
        } finally {
            connection.disconnect()
        }
    }
}
