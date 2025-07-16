package com.denicks21.speechandtext.screen

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.denicks21.speechandtext.viewmodel.VideoViewModel
import com.denicks21.speechandtext.util.KunturLogger
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MapPage(navController: NavHostController, videoViewModel: VideoViewModel) {
    val videos = videoViewModel.videos
    val context = LocalContext.current
    
    // Debug: Log videos on every recomposition
    KunturLogger.d("MapPage recomposed - Total videos: ${videos.size}", "MAP_PAGE")
    videos.forEachIndexed { index, video ->
        KunturLogger.d("Video $index: ${video.name} - ${video.uri}", "MAP_PAGE")
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = MaterialTheme.colors.primary,
                shape = RoundedCornerShape(12.dp),
                elevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "🎥 Grabaciones de Seguridad",
                        style = MaterialTheme.typography.h6,
                        color = MaterialTheme.colors.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Total de grabaciones: ${videos.size}",
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.onPrimary.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón de test para debugging
            Button(
                onClick = {
                    KunturLogger.d("Test button pressed - Adding dummy video", "MAP_PAGE")
                    val testUri = "content://media/external/video/media/test_${System.currentTimeMillis()}"
                    videoViewModel.addVideo(testUri)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🧪 Test: Agregar Video Ficticio")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (videos.isEmpty()) {
                // Estado vacío mejorado
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    backgroundColor = MaterialTheme.colors.surface,
                    shape = RoundedCornerShape(12.dp),
                    elevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "📹",
                            style = MaterialTheme.typography.h2
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No hay grabaciones disponibles",
                            style = MaterialTheme.typography.h6,
                            color = MaterialTheme.colors.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Las grabaciones de amenazas detectadas aparecerán aquí",
                            style = MaterialTheme.typography.body2,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                // Lista de grabaciones mejorada
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(videos) { video ->
                        VideoCard(
                            video = video,
                            onDelete = {
                                KunturLogger.logUIAction("Video deleted: ${video.name}", "MAP_PAGE")
                                try {
                                    val uri = Uri.parse(video.uri)
                                    context.contentResolver.delete(uri, null, null)
                                    videoViewModel.removeVideo(video)
                                } catch (e: Exception) {
                                    KunturLogger.logError("Error deleting video: ${e.message}", "MAP_PAGE")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VideoCard(
    video: com.denicks21.speechandtext.api.VideoFile,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colors.surface,
        shape = RoundedCornerShape(12.dp),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "📹 ${video.name}",
                        style = MaterialTheme.typography.subtitle1,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colors.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Grabado: ${getCurrentTimestamp()}",
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "URI: ${video.uri.takeLast(30)}...",
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                }
                
                // Botones de acción
                Row {
                    IconButton(
                        onClick = {
                            // TODO: Implementar reproducción de video
                            KunturLogger.logUIAction("Play video: ${video.name}", "MAP_PAGE")
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Reproducir",
                            tint = MaterialTheme.colors.primary
                        )
                    }
                    IconButton(
                        onClick = onDelete
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = Color.Red
                        )
                    }
                }
            }
        }
    }
}

private fun getCurrentTimestamp(): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
    return sdf.format(Date())
}
