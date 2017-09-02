package org.kui.server.api.safe

import org.kui.security.GROUP_USER
import org.kui.security.getRecordClass
import org.kui.security.safe
import org.kui.server.api.getApiObjectMapper
import org.kui.server.rest.StreamRestProcessor
import java.io.InputStream
import java.io.OutputStream

class GetRecord : StreamRestProcessor("/api/safe/<type>/<key>", "GET", listOf(GROUP_USER)) {
    override fun process(ids: Map<String, String>, parameters: Map<String, String>, inputStream: InputStream, outputStream: OutputStream) {
        val key = ids["key"]!!
        val type = ids["type"]!!
        val record = safe.get(key, getRecordClass(type))
        getApiObjectMapper().writeValue(outputStream, record)
    }
}
