package com.denicks21.speechandtext.viewmodel


import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.denicks21.speechandtext.analyzer.domain.ITextAnalyzer
import com.denicks21.speechandtext.analyzer.domain.AnalysisResult
import com.denicks21.speechandtext.analyzer.domain.LabelMapping
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Representa una incidencia detectada: etiqueta + probabilidad.
 */
data class Incident(val label: String, val probability: Float)

/**
 * ViewModel que:
 *  1) Gestiona el texto con debounce
 *  2) Lanza inferencias ONNX
 *  3) Filtra por etiqueta top-1 para detectar “incidencias”
 *  4) Loggea siempre el resultado con tag "analisis ONNX"
 *  5) Además registra en consola el texto bruto que llega para depuración
 */
class HomeViewModel(
    app: Application,
    private val textAnalyzer: ITextAnalyzer
) : AndroidViewModel(app) {

    companion object {
        /** Etiquetas consideradas “incidencias” */
        private val INCIDENT_LABELS = setOf("anger", "disgust", "fear", "caring", "confusion")
    }

    /** Texto transcrito para la UI */
    private val _speechInput = MutableStateFlow("")
    val speechInput: StateFlow<String> = _speechInput.asStateFlow()

    /** Incidencias detectadas (solo si result.label ∈ INCIDENT_LABELS) */
    private val _incidents = MutableStateFlow<List<Incident>>(emptyList())
    val incidents: StateFlow<List<Incident>> = _incidents.asStateFlow()

    /** Flujo interno para debounce */
    private val _speechFlow = MutableSharedFlow<String>(replay = 1)

    init {
        viewModelScope.launch {
            _speechFlow
                .debounce(300)
                .filter { it.isNotBlank() }
                .collectLatest { text ->
                    try {
                        // 1) Inferencia ONNX
                        val result: AnalysisResult = textAnalyzer.analyze(text)

                        // 2) Loggea siempre el resultado con tag "analisis ONNX"
                        Log.d("analisis ONNX", "Result: label=${result.label}, score=${result.score}")

                        // 3) Filtra y emite solo si es incidencia
                        if (result.label in INCIDENT_LABELS) {
                            val inc = Incident(result.label, result.score)
                            _incidents.value = listOf(inc)
                        } else {
                            _incidents.value = emptyList()
                        }
                    } catch (_: CancellationException) {
                        // inferencia cancelada: no es un error
                    } catch (e: Exception) {
                        Log.e("analisis ONNX", "Error en inferencia mijin", e)
                    }
                }
        }
    }

    /**
     * Llamar desde la UI cuando cambie el texto.
     * Además, registramos en consola el texto bruto que llega para depuración.
     */
    fun onSpeechInputChanged(text: String) {
        // ▶ Log del texto bruto de entrada
        Log.d("analisis ONNX", "Raw input text: $text")

        // Actualiza el estado interno y emite el flujo para inferencia
        _speechInput.value = text
        _speechFlow.tryEmit(text)
    }
}


