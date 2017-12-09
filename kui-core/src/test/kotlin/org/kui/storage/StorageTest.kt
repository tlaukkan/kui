package org.kui.storage

import org.apache.log4j.xml.DOMConfigurator
import org.junit.Assert
import org.junit.Ignore
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.*

class StorageTest {

    @Test
    @Ignore
    fun testTimeValueDao() {
        DOMConfigurator.configure("log4j.xml")
        val testContainer = "host-1"
        val testKey = "/var/syslog"

        val testValue = TimeValue(Date(), "test-log-line".toByteArray())

        val testDao = TimeValueDao("test_time_value")

        testDao.add(testContainer, testKey, listOf(testValue))

        val today = LocalDate.now().atStartOfDay()
        val tomorrow = today.plusDays(1)

        val beginTime = Date.from(today.toInstant(ZoneOffset.UTC))
        val endTime = Date.from(tomorrow.toInstant(ZoneOffset.UTC))

        Assert.assertEquals(1, testDao.get(beginTime, null, endTime, listOf(testContainer), listOf(testKey)).rows.size)
    }

}