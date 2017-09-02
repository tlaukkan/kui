package org.kui.analysis

import org.apache.commons.io.IOUtils
import org.apache.log4j.xml.DOMConfigurator
import org.junit.Ignore
import org.junit.Test
import org.kui.model.LogRow
import java.util.*
import kotlin.collections.HashMap

class LogRowAnalyzerTest {

    @Test
    @Ignore
    fun testLogRowAnalyser() {
        DOMConfigurator.configure("log4j.xml")
        val originalLines = IOUtils.readLines(LogRowAnalyzerTest::class.java.classLoader.getResourceAsStream("noona-europe-web-b-1.log"))

        val lines = mutableListOf<String>()
        for (originalLine in originalLines) {
            var line = originalLine.replace(Regex("browser: .* message:"), "")
            lines.add(line)
        }

        val analyser = LogRowAnalyser()
        println(lines.size)
        for (line in lines) {
            if (!line.contains("ERROR")) {
                continue
            }
            val row = LogRow(log="test", line = line)
            analyser.train(row)
        }

        /*for (pattern in analyser.logLinePatterns["test"]!!.values) {
            println(pattern.matchCount.toString().padStart(5, ' ') + " "+ pattern.label)
        }*/

        val patternCounts = HashMap<String, Int>()
        for (line in lines) {
            val row = LogRow(log="test", line = line)
            val pattern = analyser.findPattern(row)
            if (pattern == null) {
                continue
            }
            if (!patternCounts.containsKey(pattern!!.label)) {
                patternCounts[pattern!!.label] = 0
            }
            patternCounts[pattern!!.label] = patternCounts[pattern!!.label]!! + 1
        }

        val patternCountsSorted = sortByValue(patternCounts)

        for (label in patternCountsSorted.keys) {
            println(patternCounts[label]!!.toString().padStart(5, ' ') + ":" + label)
        }

    }

    fun <K, V : Comparable<V>> sortByValue(map: Map<K, V>): Map<K, V> {
        val list = map.entries.toMutableList()
        Collections.sort<Map.Entry<K, V>>(list, Comparator.comparing<Map.Entry<K, V>, V> { o -> o.value })
        Collections.reverse(list)

        val result = LinkedHashMap<K, V>()
        for (entry in list) {
            result.put(entry.key, entry.value)
        }
        return result
    }

}