package com.denicks21.speechandtext.viewmodel

import kotlin.math.min

/**
 * Tokenizador WordPiece simplificado.
 * - Divide palabras en sub-palabras con prefijo "##" si es necesario.
 */
class WordPieceTokenizer(
    private val vocab: Map<String, Int>,
    private val unkToken: String = "[UNK]"
) {
    fun tokenize(text: String): List<String> {
        val tokens = mutableListOf<String>()
        text.trim().split(" ").forEach { word ->
            var start = 0
            val chars = word
            while (start < chars.length) {
                var end = chars.length
                var curSubstr: String? = null
                while (start < end) {
                    val substr = if (start == 0) chars.substring(start, end)
                    else "##${chars.substring(start, end)}"
                    if (vocab.containsKey(substr)) {
                        curSubstr = substr
                        break
                    }
                    end--
                }
                if (curSubstr == null) {
                    tokens.add(unkToken)
                    break
                }
                tokens.add(curSubstr)
                start = end
            }
        }
        return tokens
    }
}