package org.kui.server

import com.fasterxml.jackson.databind.ObjectMapper
import khttp.get
import khttp.post
import org.junit.Assert
import org.junit.Ignore
import org.junit.Test
import org.kui.api.model.LogBatch
import org.kui.api.model.LogLine
import org.kui.model.LogRow
import java.util.*

class ApiLogTest : ApiTest() {

    @Test
    @Ignore
    fun testPostRows() {
        val headers = mutableMapOf<String, String>()
        headers.put("Authorization", "Basic ${Base64.getEncoder().encodeToString("default.admin:password1234".toByteArray())}")

        val mapper = ObjectMapper()
        val logBatch = LogBatch("test.environment", "test.environment.type", "test.host", "test.host.type", "test.log", arrayOf(LogLine(Date(), "test-line")))
        val response = post(url = "https://127.0.0.1:8443/api/log/batch", headers = headers, data = mapper.writeValueAsString(logBatch))
        Assert.assertEquals(200, response.statusCode)
    }

    @Test
    @Ignore
    fun testLogRows() {
        val headers = mutableMapOf<String, String>()
        headers.put("Authorization", "Basic ${Base64.getEncoder().encodeToString("default.admin:password1234".toByteArray())}")

        val parameters = mutableMapOf<String, String>()
        parameters["hosts"] = "test.host"
        parameters["logs"] = "test.log"

        val response = get(url = "https://127.0.0.1:8443/api/log/rows", headers =  headers, params = parameters)
        println(response.text)

        val mapper = ObjectMapper()
        val logRows: Array<LogRow> = mapper.readValue(response.text, Array<LogRow>::class.java)
        Assert.assertTrue(logRows.size > 0)
    }

}