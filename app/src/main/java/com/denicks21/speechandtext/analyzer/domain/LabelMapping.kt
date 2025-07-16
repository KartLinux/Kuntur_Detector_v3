package com.denicks21.speechandtext.analyzer.domain

/**
 * Mapa de índices a etiquetas, tomado de id2label en config.json.
 */
object LabelMapping {
    val id2label: Map<Int, String> = mapOf(
        0 to "admiration",
        1 to "amusement",
        2 to "anger",
        3 to "annoyance",
        4 to "approval",
        5 to "caring",
        6 to "confusion",
        7 to "curiosity",
        8 to "desire",
        9 to "disappointment",
        10 to "disapproval",
        11 to "disgust",
        12 to "embarrassment",
        13 to "excitement",
        14 to "fear",
        15 to "gratitude",
        16 to "grief",
        17 to "joy",
        18 to "love",
        19 to "nervousness",
        20 to "optimism",
        21 to "pride",
        22 to "realization",
        23 to "relief",
        24 to "remorse",
        25 to "sadness",
        26 to "surprise",
        27 to "neutral"
    )
}
