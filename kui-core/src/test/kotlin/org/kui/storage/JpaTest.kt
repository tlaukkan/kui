package org.kui.storage

import org.apache.log4j.xml.DOMConfigurator
import org.junit.Assert
import org.junit.Test
import org.kui.storage.jpa.JpaKeyValueTable
import org.kui.storage.jpa.JpaTimeValueTable
import java.util.*

class JpaTest {

    @Test
    fun testTimeValueTable() {
        DOMConfigurator.configure("log4j.xml")
        val testContainer = "host-1"
        val testKey = "/var/syslog"

        val beginTime = Date()
        Thread.sleep(100)

        val testTable = JpaTimeValueTable("test_time_value")

        for (i in 1..2500) {
            testTable.insert(testContainer, testKey, listOf(
                    TimeValue(Date(), "test-log-line-$i".toByteArray())))
        }

        Thread.sleep(100)

        val endTime = Date()

        val firstResult = testTable.select(null, beginTime, endTime, listOf(testContainer), listOf(testKey))
        Assert.assertEquals(1000, firstResult.rows.size)
        Assert.assertNotNull(firstResult.nextBeginId)

        val secondResult = testTable.select(UUID.fromString(firstResult.nextBeginId), beginTime, endTime, listOf(testContainer), listOf(testKey))
        Assert.assertEquals(1000, secondResult.rows.size)
        Assert.assertNotNull(secondResult.nextBeginId)

        val thirdResult = testTable.select(UUID.fromString(secondResult.nextBeginId), beginTime, endTime, listOf(testContainer), listOf(testKey))
        Assert.assertEquals(500, thirdResult.rows.size)
        Assert.assertNull(thirdResult.nextBeginId)

        testTable.insert(testContainer, testKey, listOf(
                TimeValue(Date(), "test-log-line-2501".toByteArray())))

        Thread.sleep(100)

        val fourthResult = testTable.select(UUID.fromString(thirdResult.rows.last().id), beginTime, Date(), listOf(testContainer), listOf(testKey))
        Assert.assertEquals(1, fourthResult.rows.size)
        Assert.assertNull(fourthResult.nextBeginId)
    }

    @Test
    fun testKeyValueTable() {
        DOMConfigurator.configure("log4j.xml")
        val testKey = "test-key"
        val testType = "test-type"
        val testString = "test-bytes"
        val testBytes = testString.toByteArray()
        val testString2 = "test-bytes"
        val testBytes2 = testString2.toByteArray()

        val keyValueTable : KeyValueTable = JpaKeyValueTable()

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