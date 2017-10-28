package org.kui.storage

import org.apache.log4j.xml.DOMConfigurator
import org.junit.Assert
import org.junit.Ignore
import org.junit.Test
import org.kui.model.TimeValue
import org.kui.storage.dynamodb.DynamoDbKeyValueTable
import org.kui.storage.dynamodb.DynamoDbTimeValueTable
import org.kui.storage.jpa.JpaKeyValueTable
import java.util.*

class JpaTest {

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