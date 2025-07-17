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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.Card
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Stop
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
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
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.delay
import java.util.*

@SuppressLint("RememberReturnType")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraPreviewScreen(
    navController: NavHostController,
    threatAnalysis: ThreatAnalysisResponse? = null,
    onStopRecording: () -> Unit = {},
    videoViewModel: VideoViewModel
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val cameraRecorder = remember { CameraRecorder(context, lifecycleOwner) }

    var isRecording by remember { mutableStateOf(false) }
    var elapsedTime by remember { mutableStateOf(0) }
    var recordingComplete by remember { mutableStateOf(false) }
    var showThreatInfo by remember { mutableStateOf(false) }
    var videoUri by remember { mutableStateOf<String?>(null) }

    // Indicador animado
    val infiniteTransition = rememberInfiniteTransition()
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background)
        ) {
            // Cámara
            Box(
                modifier = Modifier
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
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                        }
                        Log.d("CameraPreview", "AndroidView factory: bindCamera")
                        cameraRecorder.bindCamera(previewView)
                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )

                if (isRecording) {
                    Box(
                        modifier = Modifier
                            .padding(16.dp)
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color.Red.copy(alpha = pulseAlpha))
                            .align(Alignment.TopEnd)
                    )
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
            }

            // Sección de amenaza o mensaje por defecto
            AnimatedVisibility(
                visible = showThreatInfo && threatAnalysis != null,
                enter = fadeIn(animationSpec = tween(500, easing = FastOutSlowInEasing)),
                exit = fadeOut(animationSpec = tween(500)),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp)
            ) {
                threatAnalysis?.let { threat ->
                    Card(
                        backgroundColor = MaterialTheme.colors.surface,
                        shape = RoundedCornerShape(16.dp),
                        elevation = 8.dp,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(Color.Red.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "⚠️", style = MaterialTheme.typography.h2)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "¡ALERTA DE AMENAZA!",
                                style = MaterialTheme.typography.h5,
                                color = Color.Red,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Card(
                                backgroundColor = MaterialTheme.colors.error.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Tipo:",
                                            style = MaterialTheme.typography.subtitle1,
                                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                                        )
                                        Text(
                                            text = threat.threat_type,
                                            style = MaterialTheme.typography.subtitle1,
                                            color = Color.Red
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Palabra clave:",
                                            style = MaterialTheme.typography.subtitle1,
                                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                                        )
                                        Text(
                                            text = threat.keyword,
                                            style = MaterialTheme.typography.subtitle1,
                                            color = Color.Red
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Justificación:",
                                style = MaterialTheme.typography.subtitle1,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = threat.justification,
                                style = MaterialTheme.typography.body1,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colors.onSurface
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            if (isRecording) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(Color.Red.copy(alpha = pulseAlpha))
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Grabando evidencia: ${20 - elapsedTime}s restantes",
                                        style = MaterialTheme.typography.body2,
                                        color = Color.Red
                                    )
                                }
                            } else if (recordingComplete) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "✓", style = MaterialTheme.typography.h6, color = Color.Green)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = "Evidencia grabada exitosamente", style = MaterialTheme.typography.body2, color = Color.Green)
                                }
                            }
                        }
                    }
                }
            }
            if (!showThreatInfo || threatAnalysis == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp)
                ) {
                    Card(
                        backgroundColor = MaterialTheme.colors.surface,
                        shape = RoundedCornerShape(16.dp),
                        elevation = 4.dp,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "🛡️", style = MaterialTheme.typography.h1)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(text = "Sistema de Monitoreo Activo", style = MaterialTheme.typography.h6, textAlign = TextAlign.Center)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = "Analizando audio en tiempo real...", style = MaterialTheme.typography.body2, color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f), textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                Log.d("CameraPreview", "FAB onClick: stopRecording")
                cameraRecorder.stopRecording()
                isRecording = false
                onStopRecording()
            },
            backgroundColor = MaterialTheme.colors.primary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Stop, contentDescription = "Detener grabación")
        }
    }

    // Efectos de lógica: grabación y temporizador
    LaunchedEffect(Unit) {
        KunturLogger.logCameraEvent("Camera screen launched", "CAMERA_SCREEN")
        cameraPermissionState.launchPermissionRequest()

        delay(1000)
        showThreatInfo = true
        KunturLogger.logCameraEvent("Threat info displayed", "CAMERA_SCREEN")

        isRecording = true
        KunturLogger.logCameraEvent("Recording started - 20 second timer", "CAMERA_SCREEN")

        for (i in 1..20) {
            delay(1000)
            elapsedTime = i
            if (i % 5 == 0) Log.d("CameraPreview", "Recording progress: ${i}s elapsed")
        }

        Log.d("CameraPreview", "20s elapsed: stopRecording")
        cameraRecorder.stopRecording()
        isRecording = false
        recordingComplete = true
        KunturLogger.logCameraEvent("Recording completed after 20 seconds", "CAMERA_SCREEN")

        delay(3000)
        KunturLogger.logNavigationEvent("CameraScreen", "HomePage", "CAMERA_SCREEN")
        onStopRecording()
    }
}
