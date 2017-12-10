package org.kui.server.modules.log.api

import com.fasterxml.jackson.databind.ObjectMapper
import org.kui.security.GROUP_USER
import org.kui.security.model.Host
import org.kui.security.model.HostRecord
import org.kui.security.Safe
import org.kui.server.api.StreamRestProcessor
import java.io.InputStream
import java.io.OutputStream

class GetHosts : StreamRestProcessor("/api/log/hosts", "GET", listOf(GROUP_USER)) {
    override fun process(ids: Map<String, String>, parameters: Map<String, String>, inputStream: InputStream, outputStream: OutputStream) {
        ObjectMapper().writeValue(outputStream, Safe.getAll(HostRecord::class.java)
                .map({ record -> Host(
                        host = record.key,
                        hostType = record.hostType,
                        environment = record.environment,
                        environmentType = record.environmentType,
                        created = record.created,
                        modified = record.modified) }))
    }
}
