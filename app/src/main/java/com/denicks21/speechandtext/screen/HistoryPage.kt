package com.denicks21.speechandtext.screen

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import java.io.File

@Composable
fun HistoryPage(navController: NavHostController) {
    val context = LocalContext.current
    var files by remember { mutableStateOf(emptyList<File>()) }
    var selectedFile by remember { mutableStateOf<File?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var fileContent by remember { mutableStateOf("") }

    // Carga los .txt de /files/SpeechAndText
    LaunchedEffect(Unit) {
        reloadFiles(context) { updated -> files = updated }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.padding(16.dp)) {
            items(files) { file ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = file.name,
                        style = MaterialTheme.typography.subtitle1,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                // Leer contenido y mostrar diálogo
                                fileContent = file.readText()
                                selectedFile = file
                                showDialog = true
                            }
                            .padding(end = 8.dp)
                    )
                    IconButton(onClick = {
                        // Borrar el archivo
                        if (file.delete()) {
                            reloadFiles(context) { updated -> files = updated }
                        } else {
                            // Podrías mostrar un Snackbar de error aquí
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Borrar archivo"
                        )
                    }
                }
            }
            if (files.isEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "No hay grabaciones aún.",
                        style = MaterialTheme.typography.body2
                    )
                }
            }
        }

        // Diálogo para mostrar contenido de archivo
        if (showDialog && selectedFile != null) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(text = selectedFile!!.name) },
                text = {
                    Text(text = fileContent)
                },
                confirmButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Cerrar")
                    }
                }
            )
        }
    }
}

// Función genérica para recargar lista de archivos
private fun reloadFiles(
    context: Context,
    onResult: (List<File>) -> Unit
) {
    val dir = File(context.getExternalFilesDir("SpeechAndText"), "")
    if (dir.exists()) {
        val list = dir.listFiles { f -> f.extension == "txt" }?.toList() ?: emptyList()
        onResult(list)
    } else {
        onResult(emptyList())
    }
}
