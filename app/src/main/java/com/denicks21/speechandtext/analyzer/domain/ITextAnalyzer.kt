package com.denicks21.speechandtext.analyzer.domain

/**
 * Contrato para cualquier analizador de texto (ONNX, nube, etc.).
 */
interface ITextAnalyzer {
    /**
     * Ejecuta la inferencia sobre [text] y devuelve un resultado tipado,
     * donde [AnalysisResult.probabilities] contiene el map de todas las
     * etiquetas con su probabilidad (ya normalizadas, p. ej. tras aplicar softmax).
     *
     * @param text El texto a analizar.
     * @return AnalysisResult con el mapa de probabilidades.
     */
    suspend fun analyze(text: String): AnalysisResult
}


/**
 * Resultado de la inferencia ONNX sobre un texto.
 *
 * @property label         Nombre de la etiqueta más probable (softmax top-1).
 * @property score         Probabilidad de [label], en rango 0..100.
 * @property probabilities Mapa índice-de-etiqueta → probabilidad (0..1) para **todas** las etiquetas.
 */
data class AnalysisResult(
    val label: String,
    val score: Float
)