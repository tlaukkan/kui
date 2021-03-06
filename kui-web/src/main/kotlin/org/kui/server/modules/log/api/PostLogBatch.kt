package org.kui.server.modules.log.api

import com.fasterxml.jackson.databind.ObjectMapper
import org.kui.api.model.LogBatch
import org.kui.security.*
import org.kui.server.api.StreamRestProcessor
import org.kui.server.modules.log.service.LogService
import java.io.InputStream
import java.io.OutputStream

class PostLogBatch : StreamRestProcessor("/api/log/batch", "POST", listOf(GROUP_USER)) {

    override fun process(ids: Map<String, String>, parameters: Map<String, String>, inputStream: InputStream, outputStream: OutputStream) {
        val mapper = ObjectMapper()
        val batch = mapper.readValue(inputStream, LogBatch::class.java)
        LogService.processLogBatch(batch, mapper)
    }

}
