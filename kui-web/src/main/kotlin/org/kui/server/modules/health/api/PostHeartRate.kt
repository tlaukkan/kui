package org.kui.server.modules.health.api

import com.fasterxml.jackson.databind.ObjectMapper
import org.kui.security.*
import org.kui.server.api.StreamRestProcessor
import org.kui.server.modules.health.model.HeartRate
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat

class PostHeartRate : StreamRestProcessor("/api/health/heartrate", "POST", listOf(GROUP_USER)) {

    private val log = LoggerFactory.getLogger(PostHeartRate::class.java.name)

    override fun process(ids: Map<String, String>, parameters: Map<String, String>, inputStream: InputStream, outputStream: OutputStream) {
        val mapper = ObjectMapper()
        val heartRate = mapper.readValue(inputStream, HeartRate::class.java)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
        heartRate.rrs!!.forEach { rr ->
            log.info(dateFormat.format(heartRate.time) + "," + heartRate.hr.toString() + "," + rr)
        }

    }

}
