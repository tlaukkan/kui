package org.kui.server.api.safe

import org.kui.security.GROUP_USER
import org.kui.security.getRecordClass
import org.kui.security.safe
import org.kui.server.api.getApiObjectMapper
import org.kui.server.rest.StreamRestProcessor
import java.io.InputStream
import java.io.OutputStream

class GetRecords : StreamRestProcessor("/api/safe/<type>", "GET", listOf(GROUP_USER)) {
    override fun process(ids: Map<String, String>, parameters: Map<String, String>, inputStream: InputStream, outputStream: OutputStream) {
        val type = ids["type"]!!
        val records = safe.getAll(getRecordClass(type))
        getApiObjectMapper().writeValue(outputStream, records)
    }
}