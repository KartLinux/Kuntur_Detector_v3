package com.denicks21.speechandtext.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.denicks21.speechandtext.api.VideoFile
import com.denicks21.speechandtext.util.KunturLogger

// VideoViewModel mejorado para gestión de grabaciones
class VideoViewModel : ViewModel() {

    private val _videos = mutableStateListOf<VideoFile>()
    val videos: List<VideoFile> = _videos

    fun addVideo(uri: String) {
        val name = extractVideoName(uri)
        val videoFile = VideoFile(uri, name)
        _videos.add(videoFile)
        KunturLogger.d("Video agregado: $name (URI: $uri)", "VIDEO_VIEW_MODEL")
        KunturLogger.d("Total de videos: ${_videos.size}", "VIDEO_VIEW_MODEL")
    }

    fun removeVideo(video: VideoFile) {
        val removed = _videos.remove(video)
        if (removed) {
            KunturLogger.d("Video eliminado: ${video.name}", "VIDEO_VIEW_MODEL")
        } else {
            KunturLogger.d("No se pudo eliminar el video: ${video.name}", "VIDEO_VIEW_MODEL")
        }
        KunturLogger.d("Total de videos restantes: ${_videos.size}", "VIDEO_VIEW_MODEL")
    }

    fun clearAllVideos() {
        _videos.clear()
        KunturLogger.d("Todos los videos eliminados", "VIDEO_VIEW_MODEL")
    }

    private fun extractVideoName(uri: String): String {
        return try {
            // Extraer nombre del archivo de la URI
            val segments = uri.split("/")
            val fileName = segments.lastOrNull() ?: "video_${System.currentTimeMillis()}"
            
            // Si el nombre no tiene extensión, agregamos .mp4
            if (!fileName.contains(".")) {
                "kuntur_security_$fileName.mp4"
            } else {
                fileName
            }
        } catch (e: Exception) {
            "kuntur_security_${System.currentTimeMillis()}.mp4"
        }
    }
}
