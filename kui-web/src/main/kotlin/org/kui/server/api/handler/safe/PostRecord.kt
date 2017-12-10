package org.kui.server.api.handler.safe

import org.kui.server.api.getApiObjectMapper
import org.kui.security.*
import org.kui.server.api.getRecordClass
import org.kui.server.api.StreamRestProcessor
import java.io.InputStream
import java.io.OutputStream

class PostRecord : StreamRestProcessor("/api/safe/<type>", "POST", listOf(GROUP_USER)) {
    override fun process(ids: Map<String, String>, parameters: Map<String, String>, inputStream: InputStream, outputStream: OutputStream) {
        val type = ids["type"]!!
        val clazz = getRecordClass(type)
        val record = getApiObjectMapper().readValue(inputStream, clazz)
        Safe.add(record)
    }
}
