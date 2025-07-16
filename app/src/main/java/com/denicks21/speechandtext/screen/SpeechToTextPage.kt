package com.denicks21.speechandtext.screen

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Save
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.denicks21.speechandtext.MainActivity
import com.denicks21.speechandtext.R

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun SpeechToTextPage(navController: NavHostController) {
    // ① Referencia al estado y métodos de MainActivity
    val activity = LocalContext.current as MainActivity
    val context  = LocalContext.current

    // ② Strings desde resources
    val title       = stringResource(R.string.title_speech_to_text)
    val cdBack      = stringResource(R.string.cd_back)
    val cdStart     = stringResource(R.string.cd_start_listening)
    val cdStop      = stringResource(R.string.cd_stop_listening)
    val cdSave      = stringResource(R.string.cd_save)
    val placeholder = stringResource(R.string.placeholder_speech)
    val manualPref  = stringResource(R.string.manual_filename_prefix)
    val toastSaved  = stringResource(R.string.toast_saved)
    val toastNoText = stringResource(R.string.toast_no_text)

    // ③ Estructura con Scaffold
    Scaffold(
        backgroundColor = MaterialTheme.colors.background,
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.Close, contentDescription = cdBack)
                    }
                }
            )
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background)
                .padding(padding)
        ) {
            // ④ Texto que se va actualizando en real time + final
            Text(
                text  = activity.speechInput.value.ifBlank { placeholder },
                color = MaterialTheme.colors.onBackground,
                style = MaterialTheme.typography.body1,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            // ⑤ Botones flotantes al pie: Start/Stop y Guardar manual
            Row(
                Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FloatingActionButton(onClick = {
                    if (activity.listening.value) activity.stopListening()
                    else activity.startListening()
                }) {
                    Icon(
                        imageVector = if (activity.listening.value) Icons.Default.Close else Icons.Default.Mic,
                        contentDescription = if (activity.listening.value) cdStop else cdStart
                    )
                }
                FloatingActionButton(onClick = {
                    if (activity.speechInput.value.isNotBlank()) {
                        // Crea el nombre de archivo uniendo el prefijo y la hora actual
                        val filename = "${manualPref}${System.currentTimeMillis()}.txt"
                        activity.writeFile(
                            filename,
                            activity.speechInput.value
                        )
                        Toast.makeText(context, toastSaved, Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, toastNoText, Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Icon(Icons.Default.Save, contentDescription = cdSave)
                }
            }
        }
    }
}


