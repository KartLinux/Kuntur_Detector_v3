package com.denicks21.speechandtext.ui

import android.Manifest
import android.annotation.SuppressLint
import android.util.Log
import android.view.ViewGroup
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Stop
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.denicks21.speechandtext.api.ThreatAnalysisResponse
import com.denicks21.speechandtext.ui.camera.CameraRecorder
import com.denicks21.speechandtext.util.KunturLogger
import com.denicks21.speechandtext.viewmodel.VideoViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.delay

@SuppressLint("RememberReturnType")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraPreviewScreen(
    navController: NavHostController,
    threatAnalysis: ThreatAnalysisResponse? = null,
    videoViewModel: VideoViewModel
) {
    val context        = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // 1️⃣ Estados de permiso
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val audioPermissionState  = rememberPermissionState(Manifest.permission.RECORD_AUDIO)

    // 2️⃣ Lógica de CameraX encapsulada
    val cameraRecorder = remember { CameraRecorder(context, lifecycleOwner) }

    // 3️⃣ Estados de UI y grabación
    var isRecording       by remember { mutableStateOf(false) }
    var elapsedTime       by remember { mutableStateOf(0) }
    var recordingComplete by remember { mutableStateOf(false) }
    var showThreatInfo    by remember { mutableStateOf(false) }
    var videoUri          by remember { mutableStateOf<String?>(null) }
    var captureReady      by remember { mutableStateOf(false) }

    // 4️⃣ Animación pulso
    val infiniteTransition = rememberInfiniteTransition()
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue   = 0.3f,
        targetValue    = 1f,
        animationSpec  = infiniteRepeatable(
            animation    = tween(1000, easing = LinearEasing),
            repeatMode   = RepeatMode.Reverse
        )
    )

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background)
        ) {
            // ── Vista previa de cámara ─────────────────────────────────────────
            Box(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                            scaleType          = PreviewView.ScaleType.FILL_CENTER
                        }
                        cameraRecorder.bindCamera(previewView) {
                            captureReady = true
                            Log.d("CameraPreview", "Capture use-case listo")
                        }
                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )

                if (isRecording) {
                    Box(
                        Modifier
                            .padding(16.dp)
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color.Red.copy(alpha = pulseAlpha))
                            .align(Alignment.TopEnd)
                    )
                    Text(
                        "Grabando: ${20 - elapsedTime}s",
                        color = Color.White,
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.TopStart)
                            .background(Color.Black.copy(alpha = 0.6f), shape = RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // ── Sección amenaza / placeholder ───────────────────────────────────
            AnimatedVisibility(
                visible = showThreatInfo && threatAnalysis != null,
                enter   = fadeIn(animationSpec = tween(500, easing = FastOutSlowInEasing)),
                exit    = fadeOut(animationSpec = tween(500)),
                modifier= Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp)
            ) {
                threatAnalysis?.let { threat ->
                    Card(
                        backgroundColor = MaterialTheme.colors.surface,
                        shape           = RoundedCornerShape(16.dp),
                        elevation       = 8.dp,
                        modifier        = Modifier.fillMaxSize()
                    ) {
                        Column(
                            Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(Color.Red.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("⚠️", style = MaterialTheme.typography.h2)
                            }
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "¡ALERTA DE AMENAZA!",
                                style     = MaterialTheme.typography.h5,
                                color     = Color.Red,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(24.dp))

                            Card(
                                backgroundColor = MaterialTheme.colors.error.copy(alpha = 0.1f),
                                shape           = RoundedCornerShape(12.dp),
                                modifier        = Modifier.fillMaxWidth()
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Row(
                                        Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            "Tipo:",
                                            style = MaterialTheme.typography.subtitle1,
                                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                                        )
                                        Text(
                                            threat.threat_type,
                                            style = MaterialTheme.typography.subtitle1,
                                            color = Color.Red
                                        )
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Row(
                                        Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            "Palabra clave:",
                                            style = MaterialTheme.typography.subtitle1,
                                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                                        )
                                        Text(
                                            threat.keyword,
                                            style = MaterialTheme.typography.subtitle1,
                                            color = Color.Red
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(16.dp))
                            Text(
                                "Justificación:",
                                style = MaterialTheme.typography.subtitle1,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                threat.justification,
                                style     = MaterialTheme.typography.body1,
                                textAlign = TextAlign.Center,
                                color     = MaterialTheme.colors.onSurface
                            )
                            Spacer(Modifier.height(24.dp))

                            if (isRecording) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(Color.Red.copy(alpha = pulseAlpha))
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "Grabando evidencia: ${20 - elapsedTime}s restantes",
                                        style = MaterialTheme.typography.body2,
                                        color = Color.Red
                                    )
                                }
                            } else if (recordingComplete) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("✓", style = MaterialTheme.typography.h6, color = Color.Green)
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "Evidencia grabada exitosamente",
                                        style = MaterialTheme.typography.body2,
                                        color = Color.Green
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (!showThreatInfo || threatAnalysis == null) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp)
                ) {
                    Card(
                        backgroundColor = MaterialTheme.colors.surface,
                        shape           = RoundedCornerShape(16.dp),
                        elevation       = 4.dp,
                        modifier        = Modifier.fillMaxSize()
                    ) {
                        Box(
                            Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🛡️", style = MaterialTheme.typography.h1)
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    "Sistema de Monitoreo Activo",
                                    style     = MaterialTheme.typography.h6,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Analizando audio en tiempo real...",
                                    style     = MaterialTheme.typography.body2,
                                    color     = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── Botón detener ───────────────────────────────────────────────
        FloatingActionButton(
            onClick = {
                Log.d("CameraPreview", "FAB clicked → stopRecording")
                try {
                    isRecording = false
                    cameraRecorder.stopRecording()
                    KunturLogger.logCameraEvent("Recording stopped by user", "CAMERA_SCREEN")
                } catch (e: Exception) {
                    Log.e("CameraPreview", "Error al detener grabación: ${e.message}")
                } finally {
                    navController.popBackStack()
                }
            },
            backgroundColor = MaterialTheme.colors.primary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Stop, contentDescription = "Detener grabación")
        }
    }

    // ── Lógica permisos, binding y grabación ─────────────────────────
    // Usamos un key único para este LaunchedEffect para garantizar que solo se ejecute una vez
    val effectKey = remember { Any() }
    
    LaunchedEffect(effectKey) {
        KunturLogger.logCameraEvent("Camera screen launched - Checking permissions", "CAMERA_SCREEN")
        
        // Pedir permisos si no están concedidos
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
            // Esperamos a que se concedan los permisos
            while (!cameraPermissionState.status.isGranted) {
                delay(500)
            }
        }
        
        if (!audioPermissionState.status.isGranted) {
            audioPermissionState.launchPermissionRequest()
            // Esperamos a que se concedan los permisos
            while (!audioPermissionState.status.isGranted) {
                delay(500)
            }
        }
        
        // Esperamos a que la cámara esté lista
        KunturLogger.logCameraEvent("Waiting for camera binding", "CAMERA_SCREEN")
        while (!captureReady) {
            delay(100)
        }
        
        // Ahora que tenemos permisos y cámara lista, procedemos
        try {
            KunturLogger.logCameraEvent("Permissions granted, camera ready", "CAMERA_SCREEN")
            
            // Mostramos info de amenaza
            delay(500)
            showThreatInfo = true
            KunturLogger.logCameraEvent("Threat info displayed", "CAMERA_SCREEN")
            
            // Esperamos un poco más para que la UI se actualice
            delay(500)
            
            // Iniciar grabación
            isRecording = true
            Log.d("CameraPreview", "Iniciando grabación de 20 segundos")
            KunturLogger.logCameraEvent("Starting video recording", "CAMERA_SCREEN")
            
            // Variable para controlar si la grabación terminó correctamente
            var recordingFinished = false
            
            // Iniciamos la grabación de manera segura
            try {
                cameraRecorder.startRecording { uri ->
                    if (uri != null) {
                        Log.d("CameraPreview", "Vídeo guardado exitosamente: $uri")
                        videoViewModel.addVideo(uri)
                        videoUri = uri
                        recordingFinished = true
                    } else {
                        Log.e("CameraPreview", "Error al guardar vídeo")
                    }
                }
                
                // Temporizador de grabación (20 segundos)
                for (i in 1..20) {
                    delay(1000)
                    elapsedTime = i
                    if (i % 5 == 0) {
                        Log.d("CameraPreview", "Grabación en progreso: ${i}/20 segundos")
                        KunturLogger.logCameraEvent("Recording progress: ${i}/20s", "CAMERA_SCREEN")
                    }
                }
                
                // Detenemos la grabación
                Log.d("CameraPreview", "Tiempo completado (20s), deteniendo grabación")
                KunturLogger.logCameraEvent("Recording time completed, stopping recording", "CAMERA_SCREEN")
                cameraRecorder.stopRecording()
                
                // Esperamos a que la grabación finalice y el callback se ejecute
                var timeoutCount = 0
                while (!recordingFinished && timeoutCount < 30) {
                    delay(100)
                    timeoutCount++
                }
                
            } catch (e: Exception) {
                Log.e("CameraPreview", "Error durante la grabación: ${e.message}", e)
                KunturLogger.logCameraEvent("ERROR during recording: ${e.message}", "CAMERA_SCREEN")
            } finally {
                // Actualizamos estado de UI
                isRecording = false
                recordingComplete = true
                KunturLogger.logCameraEvent("Recording process completed", "CAMERA_SCREEN")
                
                // Mostramos el estado final por 2 segundos antes de salir
                delay(2000)
                KunturLogger.logNavigationEvent("Returning to previous screen", "CAMERA_SCREEN") 
                navController.popBackStack()
            }
            
        } catch (e: Exception) {
            Log.e("CameraPreview", "Error crítico: ${e.message}", e)
            KunturLogger.logCameraEvent("CRITICAL ERROR: ${e.message}", "CAMERA_SCREEN")
            // En caso de error crítico, volvemos atrás después de un breve delay
            delay(1000)
            navController.popBackStack()
        }
    }

    // ── Limpieza al salir ───────────────────────────────────────────
    DisposableEffect(Unit) {
        onDispose {
            // Asegurarnos de que los recursos se liberan correctamente
            try {
                Log.d("CameraPreview", "Limpiando recursos de cámara")
                KunturLogger.logCameraEvent("Cleaning up camera resources", "CAMERA_SCREEN")
                cameraRecorder.cleanup()
                isRecording = false
            } catch (e: Exception) {
                Log.e("CameraPreview", "Error limpiando recursos: ${e.message}")
            }
        }
    }
}

