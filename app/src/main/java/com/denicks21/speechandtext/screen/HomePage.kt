package com.denicks21.speechandtext.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.denicks21.speechandtext.R
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import com.denicks21.speechandtext.ui.composables.ModeSelectionCard
import com.denicks21.speechandtext.viewmodel.Incident
import com.denicks21.speechandtext.util.KunturLogger
import com.denicks21.speechandtext.api.ThreatAnalysisResponse
import com.denicks21.speechandtext.util.rememberUserLocation
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.roundToInt


// -----------------------
// Constantes de configuración
// -----------------------

// Niveles de transparencia
const val MAIN_CARD_ALPHA = 0.3f
private const val METRIC_CARD_ALPHA = 0.3f

// Radios redondeados
private val MAIN_CARD_CORNER = 16.dp
private val METRIC_CARD_CORNER = 12.dp

// Espacios para la fila de ubicación
private val LOCATION_ICON_SIZE = 20.dp
private val LOCATION_SPACING = 8.dp

// Estado global para la ubicación
private fun getDefaultLocation(): String = "Loja, Chaguarpamba, Ecuador"
// -----------------------

@Composable
fun HomePage(
    speechInputFlow: MutableState<String>,
    isListening: Boolean,
    isManualMode: Boolean,
    threatAnalysisResponse: ThreatAnalysisResponse? = null,
    onToggleListening: () -> Unit,
    onModeChange: (Boolean) -> Unit,
    onAnalyzeClicked: () -> Unit,
) {
    // Lectura reactiva del texto transcrito
    val speechInput by speechInputFlow

    // Función para limpiar repeticiones en el texto
    fun cleanRepeatedText(text: String): String {
        if (text.isBlank()) return text

        val words = text.trim().split("\\s+".toRegex())
        if (words.size <= 1) return text

        val cleanedWords = mutableListOf<String>()
        var i = 0

        while (i < words.size) {
            val currentWord = words[i]
            cleanedWords.add(currentWord)

            // Buscar repeticiones consecutivas de la misma palabra
            var j = i + 1
            while (j < words.size && words[j].equals(currentWord, ignoreCase = true)) {
                j++
            }

            // Si encontramos repeticiones, saltar todas excepto la primera
            i = j
        }

        return cleanedWords.joinToString(" ")
    }

    // Texto limpio sin repeticiones
    val cleanSpeechInput = cleanRepeatedText(speechInput)
    
    // Estado para el texto en tiempo real (últimas 3 frases)
    val realTimeText = remember { mutableStateOf("") }
    
    // Función para mantener solo las últimas frases
    fun updateRealTimeText(newText: String) {
        if (newText.isBlank()) {
            realTimeText.value = ""
            return
        }
        
        // Dividir en frases (por puntos, signos de exclamación, interrogación)
        val sentences = newText.split(Regex("[.!?]+")).filter { it.isNotBlank() }
        
        // Mantener solo las últimas 3 frases
        val lastSentences = sentences.takeLast(3)
        realTimeText.value = lastSentences.joinToString(". ").trim()
    }
    // Obtener la ubicación del usuario
    val context = LocalContext.current
    val location by rememberUserLocation(LocalContext.current)

    // Auto-borrado del texto en tiempo real (cada 10 segundos)
    LaunchedEffect(cleanSpeechInput) {
        updateRealTimeText(cleanSpeechInput)
        
        // Si hay texto, programar borrado automático después de 10 segundos de inactividad
        if (cleanSpeechInput.isNotBlank()) {
            delay(10_000) // 10 segundos
            realTimeText.value = ""
        }
    }

    // Con BoxWithConstraints leemos el alto real disponible
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.horizontalGradient(
                    listOf(
                        MaterialTheme.colors.primary,
                        MaterialTheme.colors.primaryVariant
                    )
                )
            )
            .padding(12.dp)
    ) {
        val screenHeight = maxHeight
        val mainCardMaxHeight = screenHeight * 0.30f    // reducido a 30% de la pantalla
        val metricRowHeight   = screenHeight * 0.20f   // reducido a 20% para la fila de métricas

        // Contenido desplazable, con padding bottom para no quedar oculto
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = metricRowHeight + 16.dp) // reducido padding
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // — Ubicación —
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp) // reducido padding
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_ubication),
                    contentDescription = "Ubicación",
                    tint = MaterialTheme.colors.onPrimary,
                    modifier = Modifier.size(LOCATION_ICON_SIZE)
                )
                Spacer(modifier = Modifier.width(LOCATION_SPACING))
                Text(
                    text = location.address+" Latitud: "+location.latitude+" Longitud: "+location.longitude,
                    style = MaterialTheme.typography.overline,
                    color = MaterialTheme.colors.onPrimary
                )
            }

            // — Tarjeta principal (alto dinámico) —
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = mainCardMaxHeight)
                    .clip(RoundedCornerShape(MAIN_CARD_CORNER)),
                backgroundColor = MaterialTheme.colors.surface.copy(alpha = MAIN_CARD_ALPHA),
                elevation = 0.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp), // reducido padding
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = painterResource(
                            id = if (isListening) R.drawable.ic_kuntur_on else R.drawable.ic_kuntur_off
                        ),
                        contentDescription = null,
                        modifier = Modifier.size(48.dp), // reducido tamaño
                        tint = Color.Unspecified
                    )

                    val statusText = if (isListening) "Kuntur\na la escucha" else "Kuntur\napagado"
                    val statusColor = if (isListening) Color.Green else Color.Red
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.subtitle1, // cambiado de h6 a subtitle1
                        color = statusColor,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp) // reducido padding
                    )

                    IconButton(
                        onClick = {
                            KunturLogger.logUIAction(
                                if (isListening) "Stop listening button pressed" else "Start listening button pressed",
                                "HOME_PAGE"
                            )
                            onToggleListening()
                        },
                        modifier = Modifier
                            .size(80.dp) // reducido tamaño
                            .background(Color.Transparent, CircleShape)
                    ) {
                        Icon(
                            painter = painterResource(
                                id = if (isListening) R.drawable.ic_button_off else R.drawable.ic_button_on
                            ),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            tint = Color.Unspecified
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp)) // reducido espaciado

            // — Modo de operación (nuevo) —
            ModeSelectionCard(
                isManualMode = isManualMode,
                onModeChange = { newMode ->
                    KunturLogger.logUIAction(
                        "Mode changed to ${if (newMode) "Manual" else "Automatic"}",
                        "HOME_PAGE"
                    )
                    onModeChange(newMode)
                }
            )

            Spacer(modifier = Modifier.height(1.dp))

            // — Texto transcrito completo —
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                backgroundColor = MaterialTheme.colors.surface.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp),
                elevation = 0.dp
            ) {
                Column(
                    modifier = Modifier.padding(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 16.dp)
                            .padding(horizontal = 4.dp, vertical = 4.dp) // reducido padding
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_chart),
                            contentDescription = "Transcripción",
                            tint = MaterialTheme.colors.secondary,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Transcripción:",
                            style = MaterialTheme.typography.caption,
                            color = MaterialTheme.colors.secondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = if (cleanSpeechInput.isNotBlank()) cleanSpeechInput else "Kuntur a la acción...",
                            style = MaterialTheme.typography.body2,
                            color = MaterialTheme.colors.onSurface,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(1.dp))
        }

        // — Métricas fijas al fondo —
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(metricRowHeight)
                .align(Alignment.BottomCenter)
                .padding(bottom = 0.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Botón de alerta en modo manual o card informativa en modo automático
            if (isManualMode) {
                // Botón de alerta para modo manual
                ActionableMetricCard(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    icon = painterResource(id = android.R.drawable.ic_menu_send),
                    label = "ALERTAR",
                    value = "Enviar análisis",
                    cardAlpha = if (cleanSpeechInput.isNotBlank()) METRIC_CARD_ALPHA+ 0.5f else METRIC_CARD_ALPHA ,
                    cornerSize = METRIC_CARD_CORNER,
                    elevation = if (cleanSpeechInput.isNotBlank()) 3.dp else 0.dp,
                    isEnabled = cleanSpeechInput.isNotBlank(),
                    onClick = {
                        KunturLogger.logUIAction("ALERT button pressed in manual mode", "HOME_PAGE")
                        onAnalyzeClicked()
                    }
                )
            } else {
                // Card informativa para modo automático
                MetricCard(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .size(LOCATION_ICON_SIZE),
                    icon = painterResource(id = R.drawable.ic_notification),
                    label = "No aviable",
                    value = "Alertas",
                    cardAlpha = METRIC_CARD_ALPHA,
                    cornerSize = METRIC_CARD_CORNER,
                    elevation = 0.dp
                )
            }

            // Respuesta del servidor de análisis de amenazas
            ThreatAnalysisCard(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                threatAnalysis = threatAnalysisResponse,
                cardAlpha = METRIC_CARD_ALPHA,
                cornerSize = METRIC_CARD_CORNER,
                elevation = if (threatAnalysisResponse != null) 2.dp else 0.dp
            )

        }
    }
}

@Composable
private fun MetricCard(
    modifier: Modifier = Modifier,
    icon: Painter,
    label: String,
    value: String,
    cardAlpha: Float,
    cornerSize: Dp,
    elevation: Dp
) {
    Card(
        modifier = modifier,
        backgroundColor = MaterialTheme.colors.surface.copy(alpha = cardAlpha),
        shape = RoundedCornerShape(cornerSize),
        elevation = elevation
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(painter = icon, contentDescription = null, tint = MaterialTheme.colors.onSurface)
            Text(text = label, style = MaterialTheme.typography.caption, color = MaterialTheme.colors.onSurface)
            Text(text = value, style = MaterialTheme.typography.subtitle1, color = MaterialTheme.colors.onSurface)
        }
    }
}

@Composable
private fun ActionableMetricCard(
    modifier: Modifier = Modifier,
    icon: Painter,
    label: String,
    value: String,
    cardAlpha: Float,
    cornerSize: Dp,
    elevation: Dp,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(
            enabled = isEnabled,
            onClick = onClick
        ),
        backgroundColor = if (isEnabled)
            MaterialTheme.colors.primary.copy(alpha = cardAlpha+ 0.2f)
        else
            MaterialTheme.colors.surface.copy(alpha = cardAlpha),
        shape = RoundedCornerShape(cornerSize),
        elevation = if (isEnabled) elevation else 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = if (isEnabled)
                    MaterialTheme.colors.onSurface
                else
                    MaterialTheme.colors.onPrimary.copy(alpha = 1f)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.caption.copy(
                    color = if (isEnabled)
                        MaterialTheme.colors.onSurface
                    else
                        MaterialTheme.colors.onPrimary.copy(alpha = 0.6f)
                )
            )
            Text(
                text = value,
                style = MaterialTheme.typography.subtitle1.copy(
                    color = if (isEnabled)
                        MaterialTheme.colors.onSurface
                    else
                        MaterialTheme.colors.onPrimary.copy(alpha = 0.6f)
                )
            )
        }
    }
}

@Composable
private fun ThreatAnalysisCard(
    modifier: Modifier = Modifier,
    threatAnalysis: ThreatAnalysisResponse?,
    cardAlpha: Float,
    cornerSize: Dp,
    elevation: Dp
) {
    val isThreat = threatAnalysis?.is_threat?.equals("SI", ignoreCase = true) == true

    Card(
        modifier = modifier,
        backgroundColor = when {
            threatAnalysis == null -> MaterialTheme.colors.surface.copy(alpha = cardAlpha)
            isThreat -> MaterialTheme.colors.error.copy(alpha = cardAlpha + 0.3f)
            else -> MaterialTheme.colors.surface.copy(alpha = cardAlpha + 0.2f)
        },
        shape = RoundedCornerShape(cornerSize),
        elevation = elevation
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (threatAnalysis != null) {
                // Icono basado en si es amenaza o no
                Icon(
                    painter = painterResource(
                        id = if (isThreat)
                            R.drawable.ic_notification
                        else
                            android.R.drawable.ic_dialog_info
                    ),
                    contentDescription = null,
                    tint = if (isThreat)
                        Color.Red
                    else
                        Color.Green
                )

                // Palabra clave
                Text(
                    text = "Estado:",
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onSurface
                )
                Text(
                    text = if (isThreat) "AMENAZA" else "SEGURO",
                    style = MaterialTheme.typography.subtitle2,
                    color = if (isThreat) Color.Red else Color.Green,
                    textAlign = TextAlign.Center
                )

                // Estado de amenaza
                Text(
                    text = threatAnalysis.keyword.ifEmpty { "N/A" },
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onSurface,
                )
            } else {
                Icon(
                    painter = painterResource(id = R.drawable.ic_chart),
                    contentDescription = null,
                    tint = MaterialTheme.colors.onSurface.copy(alpha = 0.9f)
                )
                Text(
                    text = "Análisis",
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = "Pendiente",
                    style = MaterialTheme.typography.subtitle1,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}





