package org.kui.server.api.users.login

import com.fasterxml.jackson.databind.ObjectMapper
import org.kui.api.model.LogTagger
import org.kui.api.model.Tagger
import org.kui.model.LogRow
import org.kui.model.TimeValue
import org.kui.security.*
import org.kui.security.model.HostRecord
import org.kui.server.api.users.LogBatch
import org.kui.server.rest.StreamRestProcessor
import org.kui.security.model.LogRecord
import org.kui.security.model.Tag
import org.kui.server.api.logs.*
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class PostLogBatch : StreamRestProcessor("/api/log/batch", "POST", listOf(GROUP_USER)) {

    val hosts = mutableMapOf<String, HostRecord>()
    val logs = mutableMapOf<String, LogRecord>()

    private var taggers : MutableList<LogTagger>? = null
    private var lastTaggersLoadedMillis = Date().time

    fun getTaggers() : List<LogTagger> {
        if (taggers == null || System.currentTimeMillis() - lastTaggersLoadedMillis > 60 * 1000) {
            taggers = mutableListOf()
            for (tagger in Safe.getAll(Tagger::class.java)) {
                taggers!!.add(LogTagger(
                        environment = getRegex(tagger.environment),
                        host = getRegex(tagger.host),
                        log = getRegex(tagger.log),
                        pattern = getRegex(tagger.pattern),
                        tag = tagger.tag,
                        color = tagger.color
                ))
            }
            lastTaggersLoadedMillis = System.currentTimeMillis()
        }
        return taggers!!
    }

    fun getRegex(pattern: String?) : Regex? {
        val regexp: String
        if (pattern != null && pattern.length != 0) {
            if (pattern.startsWith('/') && pattern.endsWith('/')) {
                return Regex(pattern.substring(1, pattern.length - 1))
            } else {
                return Regex(pattern.replace("*", "(.*)"))
            }
        } else {
            return null
        }
    }


    override fun process(ids: Map<String, String>, parameters: Map<String, String>, inputStream: InputStream, outputStream: OutputStream) {
        val mapper = ObjectMapper()

        val batch = mapper.readValue(inputStream, LogBatch::class.java)

        val user = ContextService.getThreadContext().user

        val environment = batch.environment!!
        val host = batch.host!!
        val log = batch.log!!

        synchronized(hosts) {
            if (!hosts.containsKey(host)) {
                if (!Safe.has(host, HostRecord::class.java)) {
                    Safe.add(HostRecord(key = host, hostType = batch.hostType, environment = environment, environmentType = batch.environmentType, owner = user))
                }
                hosts[host] = Safe.get(host, HostRecord::class.java)!!
            }
            if (!user.equals(hosts[host]!!.owner)) {
                throw SecurityException("Non host $host owner updating logs: $user")
            }
        }

        synchronized(logs) {
            val logKey = "$host.${log.toLowerCase().replace(Regex("[^A-Za-z0-9]"), ".")}"
            if (!logs.containsKey(logKey)) {
                if (!Safe.has(logKey, LogRecord::class.java)) {
                    Safe.add(LogRecord(key = logKey, host = host, log = log))
                }
                logs[logKey] = Safe.get(logKey, LogRecord::class.java)!!
            }
        }

        val rowTimeValues = mutableListOf<TimeValue>()
        val tagTimeValues = mutableMapOf<String, MutableList<TimeValue>>()
        for (line in batch.lines!!) {
            val logRow = LogRow("", batch.host, "", line.time, line.line)
            tag(batch, logRow, tagTimeValues)
            val timeValue = TimeValue(line.time!!, mapper.writeValueAsBytes(logRow))
            rowTimeValues.add(timeValue)
        }

        environmentLogsDao.add(environment, log, rowTimeValues)
        hostLogsDao.add(host, log, rowTimeValues)

        val logKey = "${log.toLowerCase().replace(Regex("[^A-Za-z0-9]"), ".")}"
        for (key in tagTimeValues.keys) {
            val values : MutableList<TimeValue> = tagTimeValues[key]!!
            environmentTagsDao.add("${batch.environment}.${logKey}", key, values )
            hostTypeTagsDao.add("${batch.environment}.${batch.hostType}.${logKey}", key, values )
            logTagsDao.add("${batch.host}.${logKey}", key, values )
        }

    }

    fun tag(batch: LogBatch, logRow: LogRow,
            tagTimeValues: MutableMap<String, MutableList<TimeValue>>) {
        val line = logRow.line!!
        for (tagger in getTaggers()) {
            val environmentTypeMatch = tagger.environment != null && tagger.environment!!.containsMatchIn(batch.environmentType!!)
            val environmentMatch = tagger.environment != null && tagger.environment!!.containsMatchIn(batch.environment!!)
            val hostTypeMatch = tagger.host != null && tagger.host!!.containsMatchIn(batch.hostType!!)
            val hostMatch = tagger.host != null && tagger.host!!.containsMatchIn(batch.host!!)
            val logMatch = tagger.log != null && tagger.log!!.containsMatchIn(batch.log!!)

            if (tagger.environment != null && (!environmentMatch && !environmentTypeMatch)) continue
            if (tagger.host != null && (!hostMatch && !hostTypeMatch)) continue
            if (tagger.log != null && !logMatch) continue
            if (tagger.pattern != null && !tagger.pattern!!.containsMatchIn(line)) continue

            if (logRow.tags == null) {
                logRow.tags = arrayOf(Tag(tagger.tag, tagger.color))
            } else {
                val tags = logRow.tags!!.toMutableList()
                tags.add(Tag(tagger.tag, tagger.color))
                logRow.tags = tags.toTypedArray()
            }

            if (!tagTimeValues.containsKey(tagger.tag!!)) {
                tagTimeValues[tagger.tag!!] = mutableListOf<TimeValue>()
            }
            tagTimeValues[tagger.tag!!]!!.add(TimeValue(logRow.time!!, kotlin.ByteArray(0)))
        }
    }
}
