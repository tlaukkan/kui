package org.kui.security

import org.apache.log4j.xml.DOMConfigurator
import org.junit.Assert
import org.junit.Ignore
import org.junit.Test
import org.kui.security.model.SecurityContext
import java.util.*

class SafeTest {

    @Test
    @Ignore
    fun testSafe() {
        DOMConfigurator.configure("log4j.xml")

        ContextService.setThreadContext(SecurityContext(USER_DEFAULT_ADMIN, listOf(GROUP_USER, GROUP_ADMIN), ByteArray(0), Date()))

        val testKey = "test.key"
        val testValue = "test.value"
        val testValue2 = "test.value.2"

        val testRecord = TestRecord(testKey, testValue)

        for (key in Safe.getKeys(TestRecord::class.java)) {
            Safe.remove(key, TestRecord::class.java)
        }

        Assert.assertNull(Safe.get(testKey, TestRecord::class.java))
        Assert.assertFalse(Safe.has(testKey, TestRecord::class.java))

        Safe.add(testRecord)
        try {
            Safe.add(testRecord)
            Assert.fail("Adding same record should throw security exception.")
        } catch (e: SecurityException) {
            Assert.assertEquals("Record already exists: test.key:org.kui.security.TestRecord", e.message)
        }
        Assert.assertNotNull(Safe.get(testKey, TestRecord::class.java))
        Assert.assertTrue(Safe.has(testKey, TestRecord::class.java))
        Assert.assertEquals(testValue, Safe.get(testKey, TestRecord::class.java)!!.value)

        Assert.assertEquals(1, Safe.getKeys(TestRecord::class.java).size)

        testRecord.value = testValue2
        Safe.update(testRecord)
        Assert.assertEquals(testValue2, Safe.get(testKey, TestRecord::class.java)!!.value)

        Safe.remove(testRecord)
        Assert.assertNull(Safe.get(testKey, TestRecord::class.java))
    }


}