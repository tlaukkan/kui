package org.kui.storage

import org.apache.log4j.xml.DOMConfigurator
import org.junit.Assert
import org.junit.Ignore
import org.junit.Test
import org.kui.storage.dynamodb.DynamoDbKeyValueTable
import org.kui.storage.dynamodb.DynamoDbTimeValueTable
import java.util.*

class DynamoDbTest {

    @Test
    @Ignore
    fun testTimeValueTable() {
        DOMConfigurator.configure("log4j.xml")
        val testContainer = "host-1"
        val testKey = "/var/syslog"

        val beginTime = Date()
        Thread.sleep(100)

        val testTable = DynamoDbTimeValueTable("test_time_value")

        testTable.insert(testContainer, testKey, listOf(
                TimeValue(Date(), "test-log-line-1".toByteArray()),
                TimeValue(Date(), "test-log-line-2".toByteArray()),
                TimeValue(Date(), "test-log-line-3".toByteArray())))

        Thread.sleep(100)

        val endTime = Date()

        Assert.assertEquals(3, testTable.select(beginTime, null, endTime, listOf(testContainer), listOf(testKey)).rows.size)

        Assert.assertEquals(3, testTable.selectCount(beginTime, endTime, listOf(testContainer), listOf(testKey)).count)
    }

    @Test
    @Ignore
    fun testKeyValueTable() {
        DOMConfigurator.configure("log4j.xml")
        val testKey = "test-key"
        val testType = "test-type"
        val testString = "test-bytes"
        val testBytes = testString.toByteArray()
        val testString2 = "test-bytes"
        val testBytes2 = testString2.toByteArray()

        val keyValueTable : KeyValueTable = DynamoDbKeyValueTable()

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

        Assert.assertEquals(0, keyValueTable.getWithKeyPrefix("test:", testType).size)
    }


}