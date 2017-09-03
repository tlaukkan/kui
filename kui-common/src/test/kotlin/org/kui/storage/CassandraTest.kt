package org.kui.storage

import getCassandraCredentials
import org.apache.log4j.xml.DOMConfigurator
import org.junit.Assert
import org.junit.Ignore
import org.junit.Test
import org.kui.model.TimeValue
import org.kui.storage.cassandra.CassandraKeyValueTable
import org.kui.storage.cassandra.CassandraTimeValueTable
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.*

class CassandraTest {

    @Test
    @Ignore
    fun testTimeValueTable() {
        DOMConfigurator.configure("log4j.xml")
        val testContainer = "host-1"
        val testKey = "/var/syslog"

        val testTime = Date()
        val testValue = "test-log-line"

        val testTable = CassandraTimeValueTable("test_time_value", true)

        testTable.insert(testContainer, testKey, listOf(TimeValue(testTime, testValue.toByteArray())))

        val today = LocalDate.now().atStartOfDay()
        val tomorrow = today.plusDays(1)

        val beginTime = Date.from(today.toInstant(ZoneOffset.UTC))
        val endTime = Date.from(tomorrow.toInstant(ZoneOffset.UTC))

        Assert.assertEquals(1, testTable.select(null, beginTime, endTime, listOf(testContainer), listOf(testKey)).rows.size)
    }

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

        Assert.assertEquals(1, testDao.get(null, beginTime, endTime, listOf(testContainer), listOf(testKey)).rows.size)
    }

    @Test
    @Ignore
    fun testKeyValueTable() {
        DOMConfigurator.configure("log4j.xml")

        val keyValueTable : KeyValueTable = CassandraKeyValueTable("test_key_value")

        val testKey = "test-key"
        val testType = "test-type"
        val testString = "test-bytes"
        val testBytes = testString.toByteArray()
        val testString2 = "test-bytes"
        val testBytes2 = testString2.toByteArray()

        for (key in keyValueTable.getKeys(testType)) {
            keyValueTable.remove(key, testType)
        }

        Assert.assertNull(keyValueTable.get(testKey, testType))

        keyValueTable.add(testKey, testType, testBytes)
        Assert.assertTrue(keyValueTable.has(testKey, testType))
        Assert.assertEquals(testString, String(keyValueTable.get(testKey, testType)!!))

        Assert.assertEquals(1, keyValueTable.getKeys(testType).size)
        Assert.assertEquals(testString, String(keyValueTable.getAll(testType)[0].value))

        keyValueTable.update(testKey, testType, testBytes2)
        Assert.assertEquals(testString2, String(keyValueTable.get(testKey, testType)!!))

        keyValueTable.remove(testKey, testType)
        Assert.assertNull(keyValueTable.get(testKey, testType))

        keyValueTable.add("test:1", testType, testBytes)
        keyValueTable.add("test:2", testType, testBytes)
        keyValueTable.add("test:3", testType, testBytes)

        Assert.assertEquals(3, keyValueTable.getWithKeyPrefix("test:", testType).size)

        for (key in keyValueTable.getKeys(testType)) {
            keyValueTable.remove(key, testType)
        }
    }


}