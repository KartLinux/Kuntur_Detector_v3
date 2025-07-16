package com.denicks21.speechandtext.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.Dp
import com.denicks21.speechandtext.viewmodel.Incident
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.roundToInt


// -----------------------
// Constantes de configuración
// -----------------------

// Niveles de transparencia
private const val MAIN_CARD_ALPHA = 0.3f
private const val METRIC_CARD_ALPHA = 0.3f

// Radios redondeados
private val MAIN_CARD_CORNER = 16.dp
private val METRIC_CARD_CORNER = 12.dp

// Espacios para la fila de ubicación
private val LOCATION_ICON_SIZE = 20.dp
private val LOCATION_SPACING = 8.dp

private fun getFakeLocation(): String = "Loja, Chaguarpamba, Ecuador"
// -----------------------

@Composable
fun HomePage(
    speechInputFlow: MutableState<String>,
    isListening: Boolean,
    onToggleListening: () -> Unit,
) {
    // Lectura reactiva del texto transcrito
    val speechInput by speechInputFlow

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
            .padding(16.dp)
    ) {
        val screenHeight = maxHeight
        val mainCardMaxHeight = screenHeight * 0.45f    // hasta 40% de la pantalla
        val metricRowHeight   = screenHeight * 0.25f   // 15% para la fila de métricas

        // Contenido desplazable, con padding bottom para no quedar oculto
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = metricRowHeight + 22.dp) // evita overlape con métricas
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // — Ubicación —
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 16.dp, bottom = 0.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_ubication),
                    contentDescription = "Ubicación",
                    tint = MaterialTheme.colors.onPrimary,
                    modifier = Modifier.size(LOCATION_ICON_SIZE)
                )
                Spacer(modifier = Modifier.width(LOCATION_SPACING))
                Text(
                    text = getFakeLocation(),
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
                        .padding(24.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = painterResource(
                            id = if (isListening) R.drawable.ic_kuntur_on else R.drawable.ic_kuntur_off
                        ),
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.Unspecified
                    )

                    val statusText = if (isListening) "Kuntur\na la escucha" else "Kuntur\napagado"
                    val statusColor = if (isListening) Color.Green else Color.Red
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.h6,
                        color = statusColor,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )

                    IconButton(
                        onClick = onToggleListening,
                        modifier = Modifier
                            .size(128.dp)
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

            Spacer(modifier = Modifier.height(0.dp))

            // — Texto transcrito —
            Text(
                text = if (speechInput.isNotBlank()) speechInput else "Aquí aparecerá tu texto...",
                style = MaterialTheme.typography.body1,
                color = MaterialTheme.colors.onSurface,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // — Métricas fijas al fondo —
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(metricRowHeight)
                .align(Alignment.BottomCenter)
                .padding(bottom = 0.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MetricCard(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                icon = painterResource(id = R.drawable.ic_notification),
                label = "Última alerta",
                value = "21:39",
                cardAlpha = METRIC_CARD_ALPHA,
                cornerSize = METRIC_CARD_CORNER,
                elevation = 0.dp
            )

            // Por ejemplo, muestra el número de incidencias en un MetricCard
            MetricCard(
                modifier   = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                icon       = painterResource(id = R.drawable.ic_chart),
                label = "Incidencias",
                value = "0%",
                cardAlpha  = METRIC_CARD_ALPHA,
                cornerSize = METRIC_CARD_CORNER,
                elevation  = 0.dp
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




