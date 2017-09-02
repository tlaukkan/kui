package org.kui.server.api.logs

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.kui.log.environmentLogsDao
import org.kui.log.hostLogsDao
import org.kui.model.LogResult
import org.kui.model.LogRow
import org.kui.model.TimeValueResult
import org.kui.security.GROUP_USER
import org.kui.server.rest.StreamRestProcessor
import java.io.InputStream
import java.io.OutputStream
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.*

class GetLogRows : StreamRestProcessor("/api/log/rows", "GET", listOf(GROUP_USER)) {
    val log = LoggerFactory.getLogger(GetLogRows::class.java.name)

    override fun process(ids: Map<String, String>, parameters: Map<String, String>, inputStream: InputStream, outputStream: OutputStream) {

        val today = LocalDate.now().atStartOfDay()
        val tomorrow = today.plusDays(1)

        val beginId: UUID?
        if (parameters.containsKey("beginId") && !"null".equals(parameters["beginId"]) && parameters["beginId"]!!.length != 0) {
            beginId = UUID.fromString(parameters["beginId"])
        } else {
            beginId = null
        }

        val beginTime: Date
        if (parameters.containsKey("beginTime")) {
            beginTime = Date(parameters["beginTime"]!!.toLong())
            //println(beginTime)
        } else {
            beginTime = Date.from(today.toInstant(ZoneOffset.UTC))
        }

        val endTime: Date
        if (parameters.containsKey("endTime")) {
            endTime = Date(parameters["endTime"]!!.toLong())
            //println(endTime)
        } else {
            endTime = Date.from(tomorrow.toInstant(ZoneOffset.UTC))
        }

        //println("${beginTime}: $endTime")

        var environments: List<String> = emptyList()
        if (parameters.containsKey("environments") && parameters["environments"]!!.length > 0) {
            environments = parameters["environments"]!!.split(',')
        }

        var hosts: List<String> = emptyList()
        if (parameters.containsKey("hosts") && parameters["hosts"]!!.length > 0) {
            hosts = parameters["hosts"]!!.split(',')
        }

        var logs: List<String> = emptyList()
        if (parameters.containsKey("logs") && parameters["logs"]!!.length > 0) {
            logs = parameters["logs"]!!.split(',')
        }

        var pattern: String? = null
        if (parameters.containsKey("pattern")) {
            pattern = parameters["pattern"]!!
        }

        val regex: Regex?
        if (pattern != null && pattern.length != 0) {
            if (pattern.startsWith('/') && pattern.endsWith('/')) {
                regex = Regex(pattern.substring(1, pattern.length - 1))
            } else {
                regex = Regex(pattern.replace("*", "(.*)"))
            }
        } else {
            regex = null
        }


        val logRows = arrayListOf<LogRow>()
        val mapper = ObjectMapper()
        val result: TimeValueResult
        if (hosts.size > 0) {
            result = hostLogsDao.get(beginId, beginTime, endTime, hosts, logs)
        } else {
            result = environmentLogsDao.get(beginId, beginTime, endTime, environments, logs)
        }
        for (timeValueRow in result.rows) {
            val logRow = mapper.readValue(timeValueRow.value, LogRow::class.java)

            if (regex != null && regex.find(logRow.line!!) == null) {
                continue
            }

            logRow.id = timeValueRow.id
            logRow.log = timeValueRow.key
            logRows.add(logRow)
        }

        mapper.writeValue(outputStream, LogResult(result.endTime, result.nextBeginId, logRows.toTypedArray()))
    }
}
