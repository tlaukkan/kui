package org.kui.server.api.handler.safe

import org.kui.security.GROUP_USER
import org.kui.server.api.getRecordClass
import org.kui.security.Safe
import org.kui.server.api.StreamRestProcessor
import java.io.InputStream
import java.io.OutputStream

class DeleteRecord : StreamRestProcessor("/api/safe/<type>/<key>", "DELETE", listOf(GROUP_USER)) {
    override fun process(ids: Map<String, String>, parameters: Map<String, String>, inputStream: InputStream, outputStream: OutputStream) {
        val type = ids["type"]!!
        val clazz = getRecordClass(type)
        val key = ids["key"]!!
        Safe.remove(key, clazz)
    }
}
