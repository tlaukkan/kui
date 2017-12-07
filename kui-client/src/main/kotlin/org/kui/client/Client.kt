package org.kui.client

import com.fasterxml.jackson.databind.ObjectMapper
import org.kui.model.LogLine
import org.kui.model.LogTrack
import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.WildcardFileFilter
import org.slf4j.LoggerFactory
import org.kui.util.getProperty
import java.io.*
import java.util.*
import kotlin.streams.toList


/**
 * Created by tlaukkan on 6/29/2017.
 */
class Client {

    private val log = LoggerFactory.getLogger(Client::class.java.name)

    private val mapper = ObjectMapper()
    private val parser = LineParser()
    private var logStorageClient: LogStorageClient? = null

    private val logPatterns = getProperty("client", "logs").split(',').stream().map { log -> log.trim() }.toList()

    private val logPaths = mutableListOf<String>()
    private val logTracks = mutableMapOf<String, LogTrack>()
    private val randomAccessFiles = mutableMapOf<String, RandomAccessFile>()

    private var lineReadByteBuffer = ByteArray(1024)

    private var lastSentMillis = System.currentTimeMillis()

    init {
        loadTracks()

        for (pattern in logPatterns) {
            if (pattern.contains("\\")) {
                log.warn("Log pattern \"$pattern\" contains '\\' character. Please use exclusively '/' characters.")
                continue
            }
            if (pattern.contains("*")) {
                val indexOfLastSeparator = pattern.lastIndexOf("/")
                val directoryPath = pattern.substring(0, indexOfLastSeparator)
                val filePattern = pattern.substring(indexOfLastSeparator + 1)

                val dir = File(directoryPath)
                if (!dir.exists()) {
                    log.warn("Directory: $directoryPath did not exist. (File pattern: $filePattern)")
                    continue
                }

                val fileFilter = WildcardFileFilter(filePattern)
                val files = dir.listFiles(fileFilter as FileFilter)
                for (file in files) {
                    logPaths.add(file.absolutePath)
                    log.info("Tracking: ${file.absolutePath}")
                }
            } else {
                val file = File(pattern)
                if (!file.exists()) {
                    log.warn("Log: $pattern did not exist.") // Lets include file anyway in case it appears in the future.
                }
                if (file.isDirectory()) {
                    log.warn("Log: $pattern is directory. Please add /* to the pattern.")
                    continue
                }
                logPaths.add(file.absolutePath)
                log.info("Tracking: ${file.absolutePath}")
            }
        }

        for (logPath in logPaths) {
            if (!logTracks.contains(logPath)) {
                newLogTrack(logPath)
            }
        }
    }

    private fun newLogTrack(logPath: String) {
        val logFile = File(logPath)
        val created = getLogFileCreatedDate(logFile, null)
        val logTrack = LogTrack(logFile.absolutePath, created, Date(0L), created, 0)
        logTracks[logFile.absolutePath] = logTrack
        saveTrack(logTrack)
    }

    fun saveTrack(logTrack: LogTrack) {
        val trackDirectory = File(".track")
        if (!trackDirectory.exists()) {
            trackDirectory.mkdir()
        }

        FileUtils.write(File(trackDirectory.absolutePath + "/" + logTrack.logPath.replace('/','_').replace('\\','_').replace(':','_')), mapper.writeValueAsString(logTrack), false)
    }

    fun loadTracks() {
        val trackDirectory = File(".track")
        if (!trackDirectory.exists()) {
            trackDirectory.mkdir()
        }
        logTracks.clear()
        for (trackFile in trackDirectory.listFiles()) {
            val logTrack = mapper.readValue(trackFile, LogTrack::class.java)
            logTracks.put(logTrack.logPath, logTrack)
        }
    }

    fun getLogFileCreatedDate(logFile: File, logTrack: LogTrack?) : Date {
        if (!logFile.exists()) {
            return Date() // Return current date if log file does not currently exist.
        }
        val bufferedReader = BufferedReader(FileReader(logFile))
        try {
            var line = bufferedReader.readLine()
            if (line == null) {
                // Empty log, use log file last modified date.
                return Date(logFile.lastModified())
            }
            // Attempting to read timestamp first 100 lines.
            var n = 0
            while (true) {
                try {
                    if (n > 5) {
                        // lined checked, use log file last modified date.
                        log.warn("Unable to parse file creation timestamp from: ${logFile.absolutePath} as no lines contained parsable timestamp.")
                        break
                    }
                    n++
                    return parser.parse(line, Date(logFile.lastModified())).time!!
                } catch (e: Exception) {
                    line = bufferedReader.readLine()
                    if (line == null) {
                        log.warn("Unable to parse file creation timestamp from: ${logFile.absolutePath} as no lines contained parsable timestamp.")
                        break
                    }
                }
            }

            if (logTrack != null && System.currentTimeMillis() - logFile.lastModified() < 15 * 60 * 1000) {
                return logTrack.created // File not be reread if line timestamps cannot be parsed and file has been modified inside 15 minutes.
            } else {
                return Date(logFile.lastModified()) // End of log, use log file last modified date.
            }

        } finally {
            bufferedReader.close()
        }
    }

    fun connect() {
        logStorageClient = LogStorageClient()
    }

    fun disconnect() {
        logStorageClient = null
    }

    fun isConnected() : Boolean {
        return logStorageClient != null
    }

    fun track() {
        while(true) {
            for (logPath in logPaths) {
                trackLog(logTracks[logPath]!!, File(logPath))
                Thread.sleep(10)
            }
        }
    }

    fun trackLog(logTrack: LogTrack, logFile: File) {
        try {
            if (!logFile.exists()) {
                return // Ignore non existent log file.
            }

            if (logTrack.modified.time != logFile.lastModified() || !randomAccessFiles.containsKey(logTrack.logPath)) {
                log.debug("Creating new random access file: ${logFile.absolutePath}")
                if (logTrack.created.time != getLogFileCreatedDate(logFile, logTrack).time) {
                    newLogTrack(logFile.absolutePath) // File changed, recreate log track
                    log.info("Log track reset due to file recreated based on change in created date: ${logFile.absolutePath}")
                    return
                }

                if (randomAccessFiles.containsKey(logTrack.logPath)) {
                    randomAccessFiles.get(logTrack.logPath)!!.close()
                }


                val randomAccessFile = RandomAccessFile(logFile, "r")

                log.debug("Reading unsent changes from ${logFile.absolutePath} position: ${logTrack.filePosition}")
                try {
                    randomAccessFile.seek(logTrack.filePosition)
                } catch (eof: EOFException) {
                    newLogTrack(logFile.absolutePath) // File changed, recreate log track
                    log.info("Log track reset due to file recreated based on decrease of log lines: ${logFile.absolutePath}")
                    return
                }

                randomAccessFiles[logTrack.logPath] = randomAccessFile
            }

            var lines = mutableListOf<LogLine>()
            var n = 0
            val randomAccessFile = randomAccessFiles[logTrack.logPath]!!

            val lineFeed = '\n'.toByte()
            val carriageReturn = '\r'.toByte()
            while (true) {
                val beginIndex = randomAccessFile.filePointer
                var endOfFile = false
                while (true) {
                    try {
                        val readByte = randomAccessFile.readByte()
                        if (readByte == lineFeed) {
                            break
                        }
                        if (readByte == carriageReturn) {
                            break
                        }
                    } catch (eof: EOFException) {
                        if (randomAccessFile.filePointer - beginIndex > 0) {
                            break
                        } else {
                            endOfFile = true // Eof without anything to send
                            break
                        }
                    }
                }

                if (endOfFile) {
                    break // End of file
                }
                val endIndex = randomAccessFile.filePointer
                if (endIndex - beginIndex == 1L) {
                    continue // Empty line
                }

                val lineByteLength = (endIndex - beginIndex).toInt() - 1

                if (lineReadByteBuffer.size < lineByteLength) {
                    lineReadByteBuffer = ByteArray(lineByteLength)
                }
                randomAccessFile.seek(beginIndex)
                randomAccessFile.readFully(lineReadByteBuffer, 0, lineByteLength)
                randomAccessFile.seek(endIndex)
                logTrack.filePosition = endIndex

                val lineString = String(lineReadByteBuffer, 0, lineByteLength)

                val line = parser.parse(lineString, Date(logFile.lastModified()))
                if (line.time == null) {
                    line.time = logTrack.lastLineCreated
                } else {
                    logTrack.lastLineCreated = line.time!!
                }
                lines.add(line)

                n++

                logTrack.lineIndex = logTrack.lineIndex + 1
                if (logStorageClient == null) {
                    log.info(">>> \'${logFile.absolutePath}\' (${logTrack.lineIndex}) [$beginIndex,$endIndex] line: $line")
                } else {
                    if (n >= 1000) {
                        logStorageClient!!.insertLines(logFile.path, lines)
                        lines.clear()

                        printLinesSent(logTrack, n)

                        n = 0
                        saveTrack(logTrack)
                        break // Exit to give time to other logs.
                    }
                }
            }

            if (lines.size > 0) {
                if (logStorageClient != null) {
                    logStorageClient!!.insertLines(logFile.path, lines)
                    printLinesSent(logTrack, n)
                }
            }

            logTrack.modified = Date(logFile.lastModified())
            saveTrack(logTrack)
        } catch (e: Exception) {
            throw RuntimeException("Error storing lines from ${logFile.absolutePath}", e)
        }
    }

    private fun printLinesSent(logTrack: LogTrack, n: Int) {
        log.debug("Sent changes \'${logTrack.logPath}\' (${logTrack.lineIndex - n},${logTrack.lineIndex}=${n}). Throughput: ${ String.format("%.2f", 1.0 * n / (System.currentTimeMillis() - lastSentMillis)) } lines / ms")
        lastSentMillis = System.currentTimeMillis()
    }

    fun close() {
    }


}