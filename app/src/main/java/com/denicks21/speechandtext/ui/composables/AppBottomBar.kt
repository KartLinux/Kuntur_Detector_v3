package com.denicks21.speechandtext.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.denicks21.speechandtext.R
import com.denicks21.speechandtext.navigation.NavScreens
import com.denicks21.speechandtext.ui.theme.GradientEnd
import com.denicks21.speechandtext.ui.theme.GradientStartNavBar

@Composable
fun AppBottomBar(navController: NavController) {
    // 1) Definimos la lista de pantallas junto con el recurso drawable de su icono
    val items = listOf(
        NavScreens.HistoryPage  to R.drawable.ic_history,
        NavScreens.HomePage     to R.drawable.ic_monitor,
        NavScreens.CameraPreviewScreen to R.drawable.ic_contacts,
        NavScreens.MapPage      to R.drawable.ic_map
    )

    // 2) Obtenemos la entrada actual de navegación para saber en qué ruta estamos
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // 3) Creamos un Brush de gradiente horizontal usando los colores de nuestro tema
    val gradientBrush = Brush.horizontalGradient(
        colors = listOf(GradientStartNavBar, GradientEnd)
    )

    // 4) Contenedor principal: ocupa todo el ancho, altura fija de 56dp y aplica el degradado
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp)
            .background(brush = gradientBrush)
    ) {
        // 5) Fila para repartir los ítems de navegación de forma equitativa
        Row(
            modifier = Modifier
                .fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 6) Iteramos sobre cada par (pantalla, icono)
            items.forEach { (screen, iconRes) ->
                // Determinamos si esta pestaña está seleccionada
                val selected = (currentRoute == screen.route)

                // Texto de la etiqueta según la pantalla
                val labelText = when (screen) {
                    NavScreens.HomePage     -> "Monitor"
                    NavScreens.HistoryPage  -> "Historial"
                    NavScreens.CameraPreviewScreen -> "Camara"
                    NavScreens.MapPage      -> "Encontrar"
                    else                    -> ""
                }

                // 7) Cada caja ocupa un peso igual (1f) y responde al clic
                Box(
                    modifier = Modifier
                        .weight(1f)             // usa el mismo espacio que las demás
                        .fillMaxHeight()        // ocupa toda la altura del BottomBar
                        .clickable {            // al pulsar, navegamos si no está ya seleccionado
                            if (!selected) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationRoute!!) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // 8) Fondo circular o píldora cuando está seleccionada, transparente si no
                    Box(
                        modifier = Modifier
                            .wrapContentSize()  // ajusta su tamaño al contenido
                            .background(
                                color = if (selected)
                                    MaterialTheme.colors.primary  // color primary del tema
                                else
                                    Color.Transparent             // sin fondo si no está activa
                                ,
                                shape = CircleShape             // forma circular
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // 9) Columna centrada con icono y, si está seleccionada, la etiqueta
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // Icono de la pestaña
                            Icon(
                                painter = painterResource(id = iconRes),
                                contentDescription = null,
                                tint = if (selected)
                                    MaterialTheme.colors.onSurface    // contraste sobre primary
                                else
                                    MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.medium)
                            )
                            // Texto de etiqueta visible sólo cuando está seleccionada
                            if (selected) {
                                Text(
                                    text = labelText,
                                    color = MaterialTheme.colors.onSurface,
                                    style = MaterialTheme.typography.caption
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

