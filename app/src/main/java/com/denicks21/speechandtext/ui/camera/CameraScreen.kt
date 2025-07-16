/**
 * Pantalla de vista previa de cámara con botón para alternar cámaras.
 * Explicado paso a paso, como si fuera un cuento para un niño.
 */
package com.denicks21.speechandtext.ui.camera

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.denicks21.speechandtext.R

// -----------------------
// Constantes de estilo
// -----------------------
/**
 * PREVIEW_HEIGHT_RATIO: ¿Qué tan alta se ve la cámara?
 * CARD_ALPHA: ¿Qué tan transparente es nuestra tarjeta?
 * CARD_CORNER: ¿Cuánto de redondeado queremos los bordes?
 */
private const val PREVIEW_HEIGHT_RATIO = 0.5f  // 50%
private const val CARD_ALPHA = 0.3f            // 30%
private val CARD_CORNER = 16.dp                // 16 puntos de radio

/**
 * Función que muestra la pantalla completa de la cámara.
 * navController nos ayuda a movernos entre pantallas, como un cuento con páginas.
 */
@Composable
fun CameraPreviewScreen(navController: NavHostController) {
    // 1. ¿Tenemos permiso para usar la cámara?
    /**
     * hasPermission: recuerda si ya nos dieron permiso.
     */
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Este lanzador pide permiso cuando lo necesitamos.
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }

    /**
     * Le pedimos permiso al arrancar.
     */
    LaunchedEffect(Unit) {
        if (!hasPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }
    // Si no nos dieron permiso, no dibujamos la cámara.
    if (!hasPermission) return

    // 2. ¿Qué cámara queremos usar? Frontal o trasera.
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }

    // 3. Preparamos TODO para la cámara.
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    /**
     * bindCamera(): conecta la cámara al ciclo de vida.
     * Cuando cambies de frontal a trasera o viceversa, volvemos a llamar aquí.
     */
    fun bindCamera() {
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val selector = CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build()
            val preview = androidx.camera.core.Preview.Builder()
                .build()
                .also { it.setSurfaceProvider(previewView.surfaceProvider) }

            // Quitamos cámaras anteriores y ponemos la nueva
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                selector,
                preview
            )
        }, ContextCompat.getMainExecutor(context))
    }

    /**
     * Cada vez que lensFacing cambie, volvemos a conectar la cámara.
     */
    LaunchedEffect(lensFacing) {
        bindCamera()
    }

    // 4. Aquí dibujamos la interfaz.
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .padding(16.dp)
    ) {
        val screenHeight = maxHeight
        val previewHeight = screenHeight * PREVIEW_HEIGHT_RATIO

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            /**
             * Card 1: Vista de cámara con botón para alternar.
             */
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(previewHeight)
                    .clip(RoundedCornerShape(CARD_CORNER)),
                backgroundColor = MaterialTheme.colors.surface.copy(alpha = CARD_ALPHA),
                elevation = 0.dp
            ) {
                Box { // Caja que permite meter cosas encima
                    // Aquí va la cámara
                    AndroidView(
                        factory = { previewView },
                        modifier = Modifier.fillMaxSize()
                    )
                    /**
                     * IconButton: botón redondito para cambiar cámara.
                     */
                    IconButton(
                        onClick = {
                            // Si estaba atrás, pasamos al frente; si estaba al frente, volvemos atrás.
                            lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK)
                                CameraSelector.LENS_FACING_FRONT
                            else
                                CameraSelector.LENS_FACING_BACK
                        },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                            .size(48.dp)
                            .background(
                                color = MaterialTheme.colors.surface.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(24.dp)
                            )
                    ) {
                        // Ícono de cámara desde drawable
                        Icon(
                            painter = painterResource(id = R.drawable.ic_botoncamera),
                            contentDescription = "Cambiar cámara",
                            tint = MaterialTheme.colors.primary
                        )
                    }
                }
            }

            // Espacio entre tarjetas
            Spacer(modifier = Modifier.height(16.dp))

            /**
             * Card 2: Área para mostrar texto o métricas.
             */
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .weight(1f - PREVIEW_HEIGHT_RATIO)
                    .clip(RoundedCornerShape(CARD_CORNER)),
                backgroundColor = MaterialTheme.colors.surface.copy(alpha = CARD_ALPHA),
                elevation = 0.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Texto de ejemplo
                    Text(
                        text = "Proximamente....👨‍💻",
                        style = MaterialTheme.typography.h6,
                        color = MaterialTheme.colors.onPrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}




