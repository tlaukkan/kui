package org.kui.client

import org.junit.Assert
import org.junit.Test
import org.kui.client.parseDefaultTimestamp
import org.kui.client.parseGenericTimestamp
import org.kui.client.parseOldSyslogTimestamp
import java.text.SimpleDateFormat

class LineParserTest {

    @Test
    fun testOldSyslogTimestamp() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss.SSS")
        val line = "Aug  2 06:59:49 GoogleSoftwareUpdateAgent[475]: 2017-08-02 06:59:49.534 [main]"
        val parseResult = parseOldSyslogTimestamp(line, dateFormat.parse("2017-06-29 10:57:05.001"))
        Assert.assertNotNull(parseResult)
        Assert.assertEquals("Aug  2 06:59:49", parseResult!!.timestamp)
        Assert.assertEquals("2017-08-02 06:59:49.000", dateFormat.format(parseResult!!.date))
    }

    @Test
    fun testGenericTimestamp() {
        val line = "2017-06-29 10:57:05 Found device 06:59:49"
        val parseResult = parseGenericTimestamp(line)
        Assert.assertNotNull(parseResult)
        Assert.assertEquals("2017-06-29 10:57:05", parseResult!!.timestamp)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss")
        Assert.assertEquals("2017-06-29 10:57:05", dateFormat.format(parseResult!!.date))
    }

    @Test
    fun testDefaultTimestamp() {
        val line = "2017-06-29 10:57:05.001 Found device"
        val parseResult = parseDefaultTimestamp(line)
        Assert.assertNotNull(parseResult)
        Assert.assertEquals("2017-06-29 10:57:05.001", parseResult!!.timestamp)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss.SSS")
        Assert.assertEquals("2017-06-29 10:57:05.001", dateFormat.format(parseResult!!.date))
    }

}