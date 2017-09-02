package org.kui.analysis

import org.slf4j.LoggerFactory
import org.kui.model.LogRow
import java.util.*

data class LinePattern(
    var log: String = "",
    var tokens: Array<String> = arrayOf<String>(),
    var regexp: String = "",
    var label: String = "",
    var similarity: Int = 0,
    var matchCount: Int = 0
)

data class LineSimilarityResult(
        var similarity: Int = 0,
        var newWildcardCount: Int = 0,
        var newPatternTokens: MutableList<String> = mutableListOf<String>()
)


class LogRowAnalyser {

    private val log = LoggerFactory.getLogger(LogRowAnalyser::class.java)

    val logLinePatterns = mutableMapOf<String, TreeMap<String, LinePattern>>()

    val splitRegex = Regex("\\s+")

    fun train(row: LogRow) {
        val logPath = row.log
        val line = row.line

        val tokens = row.line!!.split(splitRegex).toMutableList()
        if (tokens.size < 4) {
            return
        }
        tokens.removeAt(0)
        tokens.removeAt(0)
        tokens.removeAt(1)
        tokens.removeAt(1)

        if (!logLinePatterns.containsKey(row.log!!)) {
            logLinePatterns[row.log!!] = TreeMap<String, LinePattern>()
        }

        var matches = false
        for (pattern in logLinePatterns[row.log!!]!!.values.toList()) {
            if (pattern.tokens!!.size != tokens.size) {
                continue
            }
            val result = similarity(pattern, tokens)
            if (result.similarity == result.newPatternTokens.size) {
                // Pattern match with exact result.
                pattern.matchCount++
                matches = true
            }
            if (result.similarity  < pattern.similarity && 100.0 * result.similarity / tokens.size > 50.0 && 100.0 * result.newWildcardCount / tokens.size < 50.0) {
                // Pattern match with lesser similarity. Adjust pattern
                val newPattern = LinePattern()
                newPattern.log = row.log!!
                newPattern.tokens = result.newPatternTokens.toTypedArray()
                newPattern.similarity = result.similarity
                updateRegexpAndLabel(newPattern)
                if (!logLinePatterns[row.log!!]!!.containsKey(newPattern.label)) {
                    logLinePatterns[row.log!!]!!.put(newPattern.label, newPattern)
                    matches = true
                    log.debug("Log pattern adjusted from: ${pattern.label} to: ${newPattern.label}")
                }
            }
        }

        if (matches) {
            return
        }

        log.debug("Log pattern added: ${tokens.toList()}.")
        // No pattern match. Create new pattern.
        val pattern = LinePattern()
        pattern.log = row.log!!
        pattern.tokens = tokens.toTypedArray()
        pattern.similarity = tokens.size
        updateRegexpAndLabel(pattern)
        logLinePatterns[row.log!!]!!.put(pattern.label, pattern)
    }

    fun findPattern(row: LogRow) : LinePattern? {
        val tokens = row.line!!.split(splitRegex).toMutableList()
        if (tokens.size < 4) {
            return null
        }

        tokens.removeAt(0)
        tokens.removeAt(0)
        tokens.removeAt(1)
        tokens.removeAt(1)

        var bestPattern: LinePattern? = null
        for (pattern in logLinePatterns[row.log!!]!!.values) {
            if (match(pattern, tokens)) {
                if (bestPattern == null || pattern.matchCount > bestPattern.matchCount) {
                    bestPattern = pattern
                }
            }
        }

        return bestPattern
    }

    fun updateRegexpAndLabel(pattern: LinePattern) {
        val regexpBuilder = StringBuilder()
        val labelBuilder = StringBuilder()
        for (token in pattern.tokens) {
            if (regexpBuilder.length != 0) {
                regexpBuilder.append("\\s+")
                labelBuilder.append(" ")
            }
            if (token.equals(".*")) {
                regexpBuilder.append(token)
                labelBuilder.append("*")
            } else {
                regexpBuilder.append(Regex.escape(token))
                labelBuilder.append(token)
            }
        }
        pattern.regexp = regexpBuilder.toString()
        pattern.label = labelBuilder.toString()
    }

    fun similarity(pattern: LinePattern, tokens: List<String>) : LineSimilarityResult {
        val result = LineSimilarityResult()
        for (i in 0..pattern.tokens.size - 1) {
            if (pattern.tokens[i].equals(".*")) {
                result.similarity++
                result.newWildcardCount++
                result.newPatternTokens.add(".*")
            } else if (pattern.tokens[i].equals(tokens[i])) {
                result.similarity++
                result.newPatternTokens.add(tokens[i])
            } else {
                result.newWildcardCount++
                result.newPatternTokens.add(".*")
            }
        }
        return result
    }

    fun match(pattern: LinePattern, tokens: List<String>) : Boolean {
        if (tokens.size != pattern.tokens.size) {
            return false
        }
        var similarity: Int = 0
        for (i in 0..pattern.tokens.size - 1) {
            if (pattern.tokens[i].equals(".*")) {
                similarity++
            } else if (pattern.tokens[i].equals(tokens[i])) {
                similarity++
            }
        }
        return similarity == tokens.size
    }

}