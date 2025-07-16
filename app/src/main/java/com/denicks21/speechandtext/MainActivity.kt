package com.denicks21.speechandtext

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.MaterialTheme
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.denicks21.speechandtext.navigation.NavGraph
import com.denicks21.speechandtext.ui.theme.SpeechAndTextTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.denicks21.speechandtext.viewmodel.HomeViewModel
import com.denicks21.speechandtext.viewmodel.SpeechToTextViewModel

/**
 * MainActivity: punto de entrada de la app.
 *
 * Controla el reconocimiento de voz continuo, la UI en Compose y el auto‐guardado de transcripciones.
 */
class MainActivity : ComponentActivity() {

    /** Estado observable que mantiene el texto reconocido hasta el momento. */
    val speechInput = mutableStateOf("")

    /** Estado observable que indica si estamos escuchando activamente. */
    val listening   = mutableStateOf(false)

    /** ViewModel para el análisis de voz a texto y detección de amenazas */
    private val speechToTextViewModel by viewModels<SpeechToTextViewModel> { SpeechToTextViewModel.Factory() }

    /** Instancia de SpeechRecognizer para procesar audio a texto. */
    private lateinit var speechRecognizer: SpeechRecognizer

    /** Intent que configura los parámetros del reconocimiento de voz. */
    private lateinit var recognizerIntent: Intent

    /**
     * Lanzador para solicitar permiso de micrófono (RECORD_AUDIO) en tiempo de ejecución.
     * Si el usuario lo deniega, muestra un Toast informativo.
     */
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted) {
                Toast.makeText(
                    this,
                    "Permiso de micrófono denegado",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    /**
     * onCreate: inicializa permisos, configuraciones y la UI.
     *
     * @param savedInstanceState Bundle con el estado previo de la Activity (si existe).
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1) Verificar y pedir permisos necesarios
        val requiredPermissions = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            //Manifest.permission.FLASHLIGHT
        )
        
        // Verificar y solicitar permisos
        requiredPermissions.forEach { permission ->
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(permission)
            }
        }

        // 2) Inicializar el ViewModel
        speechToTextViewModel.initialize(this)
        
        // 3) Configurar el SpeechRecognizer y su listener
        setupSpeechRecognizer()

        // 4) Montar la UI con Jetpack Compose
        setContent {
            AppContent()
        }

        // 5) Auto‐guardado periódico: cada 10 segundos, si estamos escuchando y hay texto
        lifecycleScope.launch {
            while (isActive) {
                delay(10_000L)
                if (listening.value && speechInput.value.isNotBlank()) {
                    val ts = System.currentTimeMillis()
                    writeFile("Kuntur<3LuisGaona_${ts}.txt", speechInput.value)
                    Toast.makeText(
                        this@MainActivity,
                        getString(R.string.toast_auto_saved),
                        Toast.LENGTH_SHORT
                    ).show()
                    
                    // Update ViewModel with the current speech input
                    speechToTextViewModel.updateSpeechInput(speechInput.value)
                }
            }
        }
    }


    /**
     * UI raíz de la app en Compose.
     *
     * Aquí aplicamos el tema, configuramos la status bar y lanzamos la navegación.
     */
    @Composable
    private fun AppContent() {
        SpeechAndTextTheme {
            // 3.1) Controlador de sistema para manipular la status bar
            val systemUiController = rememberSystemUiController()
            // Color de fondo tomado del tema
            val statusBarColor = colors.primary
            // Determina si los íconos deben ser oscuros (sobre fondo claro)
            val useDarkIcons   = colors.isLight
            // Configura la status bar con el color y el modo de íconos

            SideEffect {
                systemUiController.setStatusBarColor(
                    color     = statusBarColor,
                    darkIcons = useDarkIcons
                )
            }

            // 3.2) NavGraph: controla la navegación y pasa los estados y callbacks
            val navController = rememberNavController()
            
            // Set the navigation controller in the ViewModel
            speechToTextViewModel.navController = navController
            
            NavGraph(
                navController     = navController,
                speechInputState  = speechInput,
                listeningState    = listening,
                startListening    = { startListening() },
                stopListening     = { stopListening() },
                viewModel         = speechToTextViewModel
            )
        }
    }

    /**
     * Configura SpeechRecognizer y el RecognitionListener.
     *
     * El listener:
     * - Actualiza speechInput con resultados parciales y finales.
     * - Reinicia la escucha al terminar o si ocurre un error.
     */
    private fun setupSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onPartialResults(partial: Bundle?) {
                    // Resultado parcial: muestra lo que se va reconociendo en tiempo real
                    partial
                        ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.firstOrNull()
                        ?.let { hypothesis ->
                            speechInput.value = hypothesis
                            // Actualiza el ViewModel con el texto parcial
                            speechToTextViewModel.updateSpeechInput(hypothesis)
                        }
                }

                override fun onResults(results: Bundle?) {
                    // Resultado final: añade al texto anterior si existe
                    results
                        ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.firstOrNull()
                        ?.let { hypothesis ->
                            val newText = if (speechInput.value.isBlank()) hypothesis
                                          else "${speechInput.value} $hypothesis"
                            
                            speechInput.value = newText
                            
                            // Update ViewModel with the new text
                            speechToTextViewModel.updateSpeechInput(newText)
                        }
                    // Reiniciar escucha si sigue activo
                    if (listening.value) startListening()
                }

                override fun onEndOfSpeech() {
                    // Al terminar el habla, reinicia si estamos escuchando
                    if (listening.value) startListening()
                }

                override fun onError(error: Int) {
                    // En caso de error, reinicia escucha
                    if (listening.value) startListening()
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }

        // Intent que define el modelo y el idioma del reconocimiento
        recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
    }

    /**
     * Inicia la escucha de voz.
     */
    fun startListening() {
        listening.value = true
        speechRecognizer.startListening(recognizerIntent)
    }

    /**
     * Detiene la escucha de voz.
     */
    fun stopListening() {
        listening.value = false
        speechRecognizer.stopListening()
    }

    /**
     * Escribe texto en un archivo dentro de:
     * /Android/data/.../files/SpeechAndText/
     *
     * @param fileName Nombre del archivo a crear.
     * @param text     Contenido de texto que se guardará.
     */
    fun writeFile(fileName: String, text: String) {
        val dir = File(getExternalFilesDir("SpeechAndText"), "")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, fileName)
        BufferedWriter(FileOutputStream(file).bufferedWriter()).use { it.write(text) }
    }

    /**
     * onDestroy: libera los recursos del SpeechRecognizer.
     */
    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer.destroy()
    }
}




