package com.denicks21.speechandtext.ui.camera

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.video.VideoCapture
import androidx.camera.video.Recording
import androidx.camera.video.FallbackStrategy
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Stop
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.denicks21.speechandtext.api.ThreatAnalysisResponse
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraPreviewScreen(
    navController: NavHostController,
    threatAnalysis: ThreatAnalysisResponse? = null,
    onStopRecording: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    
    // States
    var isRecording by remember { mutableStateOf(false) }
    var videoUri by remember { mutableStateOf<String?>(null) }
    var elapsedTime by remember { mutableStateOf(0) }
    var recordingComplete by remember { mutableStateOf(false) }
    var showThreatInfo by remember { mutableStateOf(false) }
    
    // Permission state
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    
    // Camera provider
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    
    // Recording indicator animation
    val infiniteTransition = rememberInfiniteTransition()
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Start recording when the component is first displayed
    LaunchedEffect(Unit) {
        cameraPermissionState.launchPermissionRequest()
        
        // Show threat info after a brief delay
        delay(1000)
        showThreatInfo = true
        
        // Start timer for recording duration (20 seconds)
        isRecording = true
        for (i in 1..20) {
            delay(1000)
            elapsedTime = i
        }
        
        // Stop recording after 20 seconds
        isRecording = false
        recordingComplete = true
        
        // Return to previous screen after recording completes
        delay(3000)
        onStopRecording()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Camera Preview
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }

                // Setup camera when view is available
                scope.launch {
                    val cameraProvider = cameraProviderFuture.await()
                    val preview = Preview.Builder().build()
                    preview.setSurfaceProvider(previewView.surfaceProvider)
                    
                    // Setup video capture
                    val qualitySelector = QualitySelector.from(
                        Quality.HIGHEST,
                        FallbackStrategy.higherQualityOrLowerThan(Quality.SD)
                    )
                    val recorder = Recorder.Builder()
                        .setQualitySelector(qualitySelector)
                        .build()
                    val videoCapture = VideoCapture.withOutput(recorder)
                    
                    // Select front camera
                    val cameraSelector = CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                        .build()
                    
                    try {
                        cameraProvider.unbindAll()
                        val camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            videoCapture
                        )
                        
                        // Start recording
                        if (isRecording) {
                            val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.getDefault())
                                .format(System.currentTimeMillis())
                            
                            val contentValues = ContentValues().apply {
                                put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                                put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
                                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                                    put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/KunturSecurityApp")
                                }
                            }
                            
                            val mediaStoreOutput = MediaStoreOutputOptions.Builder(
                                context.contentResolver,
                                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                            ).setContentValues(contentValues).build()
                            
                            try {
                                // Preparar la grabación
                                val recordingBuilder = videoCapture.output
                                    .prepareRecording(context, mediaStoreOutput)
                                
                                // Habilitar audio si tiene permiso
                                val pendingRecording = if (hasAudioPermission(context)) {
                                    recordingBuilder.withAudioEnabled()
                                } else {
                                    recordingBuilder
                                }
                                
                                // Iniciar grabación
                                val recording: Recording = pendingRecording.start(
                                    ContextCompat.getMainExecutor(context)
                                ) { recordEvent: VideoRecordEvent ->
                                    when (recordEvent) {
                                        is VideoRecordEvent.Start -> {
                                            isRecording = true
                                        }
                                        is VideoRecordEvent.Finalize -> {
                                            if (recordEvent.hasError()) {
                                                Log.e("CameraScreen", "Video capture failed: ${recordEvent.error}")
                                            } else {
                                                videoUri = recordEvent.outputResults.outputUri.toString()
                                                Log.d("CameraScreen", "Video capture succeeded: $videoUri")
                                            }
                                            isRecording = false
                                        }
                                        else -> {
                                            // Handle other events if needed
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("CameraScreen", "Video recording failed", e)
                            }
                        }
                        
                    } catch (e: Exception) {
                        Log.e("CameraScreen", "Use case binding failed", e)
                    }
                }
                
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Recording indicator
        if (isRecording) {
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Color.Red.copy(alpha = pulseAlpha))
                    .align(Alignment.TopEnd)
            )
        }
        
        // Time remaining indicator
        if (isRecording) {
            Text(
                text = "Grabando: ${20 - elapsedTime}s",
                color = Color.White,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopStart)
                    .background(Color.Black.copy(alpha = 0.6f), shape = RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        
        // Threat information card
        AnimatedVisibility(
            visible = showThreatInfo && threatAnalysis != null,
            enter = fadeIn(animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)),
            exit = fadeOut(animationSpec = tween(durationMillis = 500)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            threatAnalysis?.let { threat ->
                Card(
                    backgroundColor = MaterialTheme.colors.surface.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(8.dp),
                    elevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "¡ALERTA DE AMENAZA!",
                            style = MaterialTheme.typography.h6,
                            color = Color.Red,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Tipo: ${threat.threat_type}",
                            style = MaterialTheme.typography.subtitle1
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = "Palabra clave: ${threat.keyword}",
                            style = MaterialTheme.typography.subtitle1
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = threat.justification,
                            style = MaterialTheme.typography.body2,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
        
        // Stop recording button
        Box(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.BottomEnd)
        ) {
            FloatingActionButton(
                onClick = {
                    isRecording = false
                    onStopRecording()
                },
                backgroundColor = MaterialTheme.colors.primary
            ) {
                Icon(Icons.Default.Stop, contentDescription = "Detener grabación")
            }
        }
    }
}

fun hasAudioPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.RECORD_AUDIO
    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
}

suspend fun Context.getCameraProvider(): ProcessCameraProvider = suspendCoroutine { continuation ->
    ProcessCameraProvider.getInstance(this).also { future ->
        future.addListener(
            {
                continuation.resume(future.get())
            },
            ContextCompat.getMainExecutor(this)
        )
    }
}
