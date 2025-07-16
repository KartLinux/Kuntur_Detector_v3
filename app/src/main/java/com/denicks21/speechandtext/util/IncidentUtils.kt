package com.denicks21.speechandtext.util

import com.denicks21.speechandtext.analyzer.domain.AnalysisResult
import com.denicks21.speechandtext.viewmodel.Incident
import kotlin.math.roundToInt

/**
 * Utilidades para detectar, contar y formatear incidencias.
 */
object IncidentUtils {
    /** Etiquetas consideradas incidencias */
    private val INCIDENT_LABELS = setOf(
        "anger",
        "disgust",
        "fear",
        "caring",
        "confusion"
    )

    /**
     * Comprueba si [label] es una incidencia de interés.
     * @return true si [label] está en el conjunto de incidencias.
     */
    fun isIncidentLabel(label: String): Boolean = label in INCIDENT_LABELS

    /**
     * Convierte un [result] de análisis en un [Incident] si corresponde.
     */
    fun extractIncident(result: AnalysisResult): Incident? =
        if (isIncidentLabel(result.label)) Incident(result.label, result.score)
        else null

    /**
     * Actualiza el conteo de incidencias dado un [incident].
     * @return Nuevo mapa con el conteo incrementado.
     */
    fun updateCounts(
        counts: Map<String, Int>,
        incident: Incident?
    ): Map<String, Int> =
        incident?.let {
            counts + (it.label to (counts[it.label] ?: 0) + 1)
        } ?: counts

    /**
     * Devuelve la etiqueta de [result] como String.
     */
    fun formatLabel(result: AnalysisResult): String = result.label

    /**
     * Devuelve el score de [result] formateado como porcentaje, por ejemplo "76%".
     */
    fun formatScore(result: AnalysisResult): String = "${result.score.roundToInt()}%"

    /**
     * Devuelve el conteo total de todas las incidencias como String.
     */
    fun formatTotalCount(counts: Map<String, Int>): String = counts.values.sum().toString()

    /**
     * Devuelve el conteo para una etiqueta específica como String.
     */
    fun formatCountForLabel(counts: Map<String, Int>, label: String): String =
        counts[label]?.toString() ?: "0"
}
