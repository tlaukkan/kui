package org.kui.client.tracker

import org.kui.model.LogLine
import org.pojava.datetime.DateTime
import org.slf4j.LoggerFactory
import org.kui.util.getProperty
import java.text.SimpleDateFormat
import java.util.*

/**
 * Log line parser.
 */
class LineParser {

    private val log = LoggerFactory.getLogger(LogStorageClient::class.java.name)


    fun parse(line: String, fileLastModified: Date): LogLine {
        if (line.startsWith(' ')|| line.startsWith('\t')) {
            return LogLine(line = line, time = null)
        }

        try {
            var parseResult = parseDefaultTimestamp(line)
            if (parseResult == null) {
                parseResult = parseOldSyslogTimestamp(line, fileLastModified)
            }
            if (parseResult == null) {
                parseResult = parseGenericTimestamp(line)
            }
            if (parseResult == null) {
                log.warn("No timestamp parser: $line.")
                return LogLine(line = line, time = null)
            }

            var content = line.substring(parseResult.timestamp.length)
            if (content.startsWith(" ")) {
                content = content.substring(1)
            } else if (content.startsWith(", ")) {
                content = content.substring(1)
            }

            if (getProperty("client","simulate").equals("true")) {
                log.debug("Parsed line timestamp to date time value: ${parseResult.date}")
            }
            return LogLine(line = content, time = parseResult.date)
        } catch (e: Exception) {
            log.warn("Timestamp parsing failed: $line.", e)
            return LogLine(line = line, time = null)
        }
    }
}

val defaultTimestampRegex = Regex("\\A[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}\\.[0-9]{3}")
private val defaultTimestampFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")

fun parseDefaultTimestamp(line: String) : ParseResult? {
    val match = defaultTimestampRegex.find(line, 0) ?: return null
    val timestamp = match!!.value
    val date = defaultTimestampFormat.parse(timestamp)
    return ParseResult(timestamp, date)
}

val timeRegex = Regex("([0-9]{2}:){2}[0-9]{2}")
fun parseGenericTimestamp(line: String) : ParseResult? {
    val timestampCandidate = line.substring(0, line.indexOf(':') + 6)
    val match = timeRegex.find(timestampCandidate, 0) ?: return null

    val timeString = match!!.value
    val timeEndIndex = line.indexOf(timeString) + timeString.length
    val timestamp = line.substring(0, timeEndIndex)
    val date = DateTime(timestamp).toDate()

    return ParseResult(timestamp, date)
}

val oldSyslogTimestampRegex = Regex("\\A[a-zA-Z]*\\s*[0-9]{1,2} [0-9]{2}:[0-9]{2}:[0-9]{2}")
private val oldSyslogTimestampFormat = SimpleDateFormat("yyyy MMM d HH:mm:ss")
fun parseOldSyslogTimestamp(line: String, fileLastModified: Date) : ParseResult? {
    val match = oldSyslogTimestampRegex.find(line, 0) ?: return null
    val timestamp = match!!.value
    val date = oldSyslogTimestampFormat.parse("${(fileLastModified.year + 1900)} $timestamp")
    return ParseResult(timestamp, date)
}
