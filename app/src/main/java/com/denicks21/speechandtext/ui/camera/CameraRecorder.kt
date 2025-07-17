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
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.camera.video.Recording

private const val TAG = "CameraRecorder"

/**
 * CameraRecorder encapsula toda la lógica de CameraX para grabar vídeo.
 * - bindCamera(): configura CameraX con Preview y VideoCapture.
 * - startRecording(): inicia la grabación al MediaStore.
 * - stopRecording(): detiene la grabación activa.
 */
class CameraRecorder(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner
) {
    private var videoCaptureUseCase: VideoCapture<Recorder>? = null
    private var activeRecording: Recording? = null
    private val executor = ContextCompat.getMainExecutor(context)

    /**
     * Bindea Preview y VideoCapture al lifecycleOwner.
     * Debe llamarse antes de startRecording().
     */
    fun bindCamera(previewView: PreviewView) {
        Log.d(TAG, "bindCamera: inicializando binding de cámara")
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                // Configurar Preview use-case
                val preview = Preview.Builder()
                    .build().also { it.setSurfaceProvider(previewView.surfaceProvider) }

                // Configurar Recorder con calidad deseada
                val recorder = Recorder.Builder()
                    .setQualitySelector(
                        QualitySelector.from(
                            Quality.HIGHEST,
                            FallbackStrategy.higherQualityOrLowerThan(Quality.SD)
                        )
                    ).build()

                // Crear VideoCapture use-case
                videoCaptureUseCase = VideoCapture.withOutput(recorder)

                // Unbind de casos previos y bind al lifecycle
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_FRONT_CAMERA,
                    preview,
                    videoCaptureUseCase
                )
                Log.d(TAG, "bindCamera: binding exitoso")
            } catch (e: Exception) {
                Log.e(TAG, "bindCamera: error bindeando cámara", e)
            }
        }, executor)
    }

    /**
     * Inicia la grabación de vídeo.
     * @param onResult Callback que recibe el URI grabado o null si hubo error.
     */
    fun startRecording(onResult: (uri: String?) -> Unit) {
        Log.d(TAG, "startRecording: preparando grabación")
        val vc = videoCaptureUseCase
        if (vc == null) {
            Log.e(TAG, "startRecording: use-case no inicializado")
            onResult(null)
            return
        }

        // Nombre de archivo con timestamp
        val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.getDefault())
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Download/KunturVideos")
            }
        }

        val outputOptions = MediaStoreOutputOptions
            .Builder(context.contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            .setContentValues(contentValues)
            .build()

        try {
            var builder = vc.output.prepareRecording(context, outputOptions)
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED) {
                builder = builder.withAudioEnabled()
                Log.d(TAG, "startRecording: audio habilitado")
            }
            activeRecording = builder.start(executor) { recordEvent: VideoRecordEvent ->
                when (recordEvent) {
                    is VideoRecordEvent.Start -> Log.d(TAG, "startRecording: evento START")
                    is VideoRecordEvent.Finalize -> {
                        if (recordEvent.hasError()) {
                            Log.e(TAG, "startRecording: error durante grabación: ${recordEvent.error}")
                            onResult(null)
                        } else {
                            val uri = recordEvent.outputResults.outputUri.toString()
                            Log.d(TAG, "startRecording: grabación finalizada URI=$uri")
                            onResult(uri)
                        }
                        activeRecording = null
                    }
                    else -> {}
                }
            }
            Log.d(TAG, "startRecording: llamada a .start() completada")
        } catch (e: Exception) {
            Log.e(TAG, "startRecording: excepción iniciando grabación", e)
            onResult(null)
        }
    }

    /**
     * Detiene la grabación activa.
     */
    fun stopRecording() {
        Log.d(TAG, "stopRecording: solicitando stop en activeRecording")
        activeRecording?.stop()
    }
}
