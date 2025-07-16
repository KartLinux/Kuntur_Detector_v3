package com.denicks21.speechandtext.analyzer.domain.analysis

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import android.util.Log

/**
 * Analiza cada fotograma recibido de la cámara y lo pasa a quien lo necesite.
 *
 * @param onFrame callback que recibe cada ImageProxy para procesarlo fuera de aquí.
 */
class FrameAnalyzer(
    private val onFrame: (ImageProxy) -> Unit
) : ImageAnalysis.Analyzer {

    override fun analyze(image: ImageProxy) {
        // 1. Opcional: loguear dimensiones y formato para verificar que llegan fotogramas
        Log.d("FrameAnalyzer", "Fotograma recibido: ${image.width}x${image.height}, formato=${image.format}")

        // 2. Llamamos al callback externo para que otra clase lo procese
        onFrame(image)

        // 3. Muy importante: cerrar el ImageProxy para liberar la cámara
        image.close()
    }
}