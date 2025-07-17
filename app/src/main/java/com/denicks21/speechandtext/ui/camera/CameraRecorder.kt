package com.denicks21.speechandtext.ui.camera

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.video.Recording
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.denicks21.speechandtext.util.KunturLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executor

private const val TAG = "CameraRecorder"

/**
 * Encapsula la lógica de CameraX para preview y grabación de vídeo.
 * Versión optimizada con mejor manejo de errores y concurrencia.
 */
class CameraRecorder(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner
) {
    // Use-cases de CameraX
    private var cameraProvider: ProcessCameraProvider? = null
    private var videoCaptureUseCase: VideoCapture<Recorder>? = null
    private var activeRecording: Recording? = null
    
    // Estado de grabación
    @Volatile
    private var isRecordingActive = false
    
    // Executor para eventos de CameraX
    private val mainExecutor: Executor = ContextCompat.getMainExecutor(context)

    /**
     * Configura Preview + VideoCapture y llama onReady() cuando el use-case ya está activo.
     * Implementación mejorada con mejor manejo de errores.
     */
    fun bindCamera(previewView: PreviewView, onReady: () -> Unit) {
        KunturLogger.log("Iniciando configuración de cámara", "CAMERA_FLOW")
        Log.d(TAG, "bindCamera: inicializando binding de cámara")
        
        // Limpiamos cualquier estado previo
        cleanup()
        
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                // Obtenemos el proveedor de cámara
                val provider = cameraProviderFuture.get()
                cameraProvider = provider
                
                // 1. Configuramos el preview
                val preview = Preview.Builder()
                    .build()
                    .also { 
                        it.setSurfaceProvider(previewView.surfaceProvider) 
                    }

                // 2. Configuramos el recorder para video
                val recorder = Recorder.Builder()
                    .setQualitySelector(
                        QualitySelector.from(
                            Quality.HD, // Calidad específica para mayor consistencia
                            FallbackStrategy.higherQualityOrLowerThan(Quality.SD)
                        )
                    )
                    .build()

                // 3. Creamos el use-case de VideoCapture
                val videoCapture = VideoCapture.withOutput(recorder)
                
                // 4. Desvinculamos casos de uso anteriores
                provider.unbindAll()
                
                // 5. Vinculamos los nuevos casos de uso al ciclo de vida
                try {
                    provider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_FRONT_CAMERA,
                        preview,
                        videoCapture
                    )
                    
                    // Guardamos referencia al use-case de video
                    videoCaptureUseCase = videoCapture
                    Log.d(TAG, "bindCamera: cámara configurada correctamente")
                    KunturLogger.log("Cámara configurada correctamente", "CAMERA_FLOW")
                    
                    // Notificamos que está lista
                    onReady()
                } catch (e: Exception) {
                    Log.e(TAG, "Error al vincular casos de uso de cámara", e)
                    KunturLogger.log("Error al vincular casos de uso: ${e.message}", "CAMERA_FLOW")
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "bindCamera: error crítico al configurar cámara", e)
                KunturLogger.log("Error crítico de cámara: ${e.message}", "CAMERA_FLOW")
            }
        }, mainExecutor)
    }

    /**
     * Inicia la grabación de vídeo de manera segura, con múltiples verificaciones
     * para evitar errores de muxer y garantizar que el resultado se entregue correctamente.
     * 
     * @param onResult Callback con el URI resultante, o null en caso de error.
     */
    @Synchronized
    fun startRecording(onResult: (uri: String?) -> Unit) {
        if (isRecordingActive) {
            Log.w(TAG, "Hay una grabación en progreso, no se puede iniciar otra")
            KunturLogger.log("Intento de grabación rechazado - ya hay una grabación activa", "CAMERA_FLOW")
            return
        }
        
        // Marcamos como iniciando la grabación
        isRecordingActive = true
        KunturLogger.log("Iniciando grabación de video", "CAMERA_FLOW")
        
        // Limpiamos cualquier grabación anterior
        if (activeRecording != null) {
            try {
                stopRecordingSafely()
                // Esperamos para asegurar que se haya limpiado
                Thread.sleep(300)
            } catch (e: Exception) {
                Log.e(TAG, "Error limpiando grabación anterior", e)
            }
            activeRecording = null
        }

        val videoCapture = videoCaptureUseCase
        if (videoCapture == null) {
            Log.e(TAG, "startRecording: use-case de video no inicializado")
            KunturLogger.log("Error: use-case de video no inicializado", "CAMERA_FLOW")
            isRecordingActive = false
            onResult(null)
            return
        }

        try {
            // 1. Generamos un nombre único de archivo
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.getDefault())
                .format(System.currentTimeMillis())
            val displayName = "kuntur_video_$timestamp"
            
            Log.d(TAG, "Preparando grabación: $displayName")
            KunturLogger.log("Preparando grabación: $displayName", "CAMERA_FLOW")
            
            // 2. Configuramos el destino del archivo
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                    put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/KunturApp")
                }
            }
            
            val outputOptions = MediaStoreOutputOptions.Builder(
                context.contentResolver, 
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            )
            .setContentValues(contentValues)
            .build()

            // 3. Preparamos la grabación
            var builder = videoCapture.output.prepareRecording(context, outputOptions)
            
            // 4. Habilitamos audio si tenemos permiso
            if (ContextCompat.checkSelfPermission(
                    context, Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                builder = builder.withAudioEnabled()
                Log.d(TAG, "Audio habilitado para la grabación")
                KunturLogger.log("Audio habilitado para grabación", "CAMERA_FLOW")
            }

            // 5. Iniciamos la grabación con manejo robusto de eventos
            // PendingRecording no tiene método build() - builder ya es el PendingRecording
            
            // Iniciamos la grabación con retardo
            GlobalScope.launch(Dispatchers.Main) {
                try {
                    // Pequeña pausa antes de iniciar para evitar problemas de concurrencia
                    delay(200)
                    
                    // Iniciamos la grabación directamente con el builder (PendingRecording)
                    activeRecording = builder.start(mainExecutor) { event ->
                        when (event) {
                            is VideoRecordEvent.Start -> {
                                Log.d(TAG, "Evento START recibido - Grabación iniciada")
                                KunturLogger.log("Grabación iniciada correctamente", "CAMERA_FLOW")
                            }
                            
                            is VideoRecordEvent.Status -> {
                                // Eventos de estado, útiles para debugging
                                val duration = event.recordingStats.recordedDurationNanos / 1_000_000
                                if (duration % 5000 == 0L) { // Log cada 5 segundos
                                    Log.d(TAG, "Grabación en progreso: $duration ms")
                                }
                            }
                            
                            is VideoRecordEvent.Finalize -> {
                                if (event.hasError()) {
                                    Log.e(TAG, "Error finalizando grabación: ${event.error}")
                                    KunturLogger.log("Error al finalizar grabación: ${event.error}", "CAMERA_FLOW")
                                    onResult(null)
                                } else {
                                    val uri = event.outputResults.outputUri.toString()
                                    Log.d(TAG, "Grabación finalizada exitosamente: $uri")
                                    KunturLogger.log("Video guardado: $uri", "CAMERA_FLOW")
                                    onResult(uri)
                                }
                                // Limpieza
                                activeRecording = null
                                isRecordingActive = false
                            }
                        }
                    }
                    
                    KunturLogger.log("Solicitud de grabación procesada", "CAMERA_FLOW")
                } catch (e: Exception) {
                    Log.e(TAG, "Error crítico al iniciar grabación", e)
                    KunturLogger.log("Error crítico al iniciar grabación: ${e.message}", "CAMERA_FLOW")
                    activeRecording = null
                    isRecordingActive = false
                    onResult(null)
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error preparando la grabación", e)
            KunturLogger.log("Error preparando grabación: ${e.message}", "CAMERA_FLOW")
            activeRecording = null
            isRecordingActive = false
            onResult(null)
        }
    }

    /**
     * Detiene la grabación activa de manera segura.
     */
    @Synchronized
    fun stopRecording() {
        stopRecordingSafely()
    }
    
    /**
     * Implementación interna para detener la grabación con manejo de errores.
     */
    private fun stopRecordingSafely() {
        Log.d(TAG, "Deteniendo grabación...")
        KunturLogger.log("Solicitando detener grabación", "CAMERA_FLOW")
        
        try {
            val recording = activeRecording
            if (recording != null) {
                recording.stop()
                Log.d(TAG, "Solicitud de detención enviada correctamente")
                KunturLogger.log("Grabación detenida correctamente", "CAMERA_FLOW")
            } else {
                Log.d(TAG, "No hay grabación activa para detener")
                KunturLogger.log("No hay grabación activa para detener", "CAMERA_FLOW")
                isRecordingActive = false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al detener grabación", e)
            KunturLogger.log("Error al detener grabación: ${e.message}", "CAMERA_FLOW")
            isRecordingActive = false
        }
    }
    
    /**
     * Limpia todos los recursos de la cámara.
     */
    fun cleanup() {
        try {
            // Detener cualquier grabación activa
            stopRecordingSafely()
            
            // Limpiar referencias
            activeRecording = null
            videoCaptureUseCase = null
            
            // Desenlazar todos los casos de uso
            cameraProvider?.unbindAll()
            
            Log.d(TAG, "Recursos de cámara liberados")
            KunturLogger.log("Recursos de cámara liberados", "CAMERA_FLOW")
        } catch (e: Exception) {
            Log.e(TAG, "Error limpiando recursos", e)
        }
    }
}
