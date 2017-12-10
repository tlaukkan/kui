package org.kui.server.modules.log.api

import com.fasterxml.jackson.databind.ObjectMapper
import org.kui.security.GROUP_USER
import org.kui.security.model.Log
import org.kui.security.model.LogRecord
import org.kui.security.Safe
import org.kui.server.api.StreamRestProcessor
import java.io.InputStream
import java.io.OutputStream

class GetLogs : StreamRestProcessor("/api/log/logs", "GET", listOf(GROUP_USER)) {
    override fun process(ids: Map<String, String>, parameters: Map<String, String>, inputStream: InputStream, outputStream: OutputStream) {
        ObjectMapper().writeValue(outputStream, Safe.getAll(LogRecord::class.java)
                .map({ record -> Log(
                        host = record.host,
                        log = record.log,
                        created = record.created,
                        modified = record.modified) }))
    }
}
