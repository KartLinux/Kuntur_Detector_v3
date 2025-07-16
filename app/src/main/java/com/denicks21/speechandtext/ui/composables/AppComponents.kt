package com.denicks21.speechandtext.ui.composables
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.denicks21.speechandtext.screen.MAIN_CARD_ALPHA

// -----------------------
@Composable
fun ModeSelectionCard(
    isManualMode: Boolean,
    onModeChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        elevation = 0.dp,
        backgroundColor = MaterialTheme.colors.surface.copy(alpha = MAIN_CARD_ALPHA),
        shape = RoundedCornerShape(8.dp), // reducido corner radius
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Modo de operación",
                style = MaterialTheme.typography.subtitle1
            )

            Spacer(modifier = Modifier.height(0.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isManualMode) "Manual" else "Automático",
                    style = MaterialTheme.typography.body1
                )

                Switch(
                    checked = isManualMode,
                    onCheckedChange = onModeChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colors.primary,
                        checkedTrackColor = MaterialTheme.colors.primaryVariant
                    )
                )
            }

            Spacer(modifier = Modifier.height(0.dp))

            Text(
                text = if (isManualMode)
                    "Transcripción continua con análisis manual"
                else
                    "Transcripción y análisis automático cada 20 segundos",
                style = MaterialTheme.typography.caption
            )
        }
    }
}




