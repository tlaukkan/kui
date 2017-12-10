package org.kui.server.api.handler.safe

import org.kui.security.GROUP_USER
import org.kui.server.api.getRecordClass
import org.kui.security.Safe
import org.kui.server.api.getApiObjectMapper
import org.kui.server.api.StreamRestProcessor
import java.io.InputStream
import java.io.OutputStream

class GetRecord : StreamRestProcessor("/api/safe/<type>/<key>", "GET", listOf(GROUP_USER)) {
    override fun process(ids: Map<String, String>, parameters: Map<String, String>, inputStream: InputStream, outputStream: OutputStream) {
        val key = ids["key"]!!
        val type = ids["type"]!!
        val record = Safe.get(key, getRecordClass(type))
        getApiObjectMapper().writeValue(outputStream, record)
    }
}
