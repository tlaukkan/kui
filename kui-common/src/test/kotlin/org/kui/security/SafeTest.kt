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

        contextService.setThreadContext(SecurityContext(USER_DEFAULT_ADMIN, listOf(GROUP_USER, GROUP_ADMIN), ByteArray(0), Date()))

        val testKey = "test.key"
        val testValue = "test.value"
        val testValue2 = "test.value.2"

        val testRecord = TestRecord(testKey, testValue)

        for (key in safe.getKeys(TestRecord::class.java)) {
            safe.remove(key, TestRecord::class.java)
        }

        Assert.assertNull(safe.get(testKey, TestRecord::class.java))
        Assert.assertFalse(safe.has(testKey, TestRecord::class.java))

        safe.add(testRecord)
        Assert.assertNotNull(safe.get(testKey, TestRecord::class.java))
        Assert.assertTrue(safe.has(testKey, TestRecord::class.java))
        Assert.assertEquals(testValue, safe.get(testKey, TestRecord::class.java)!!.value)

        Assert.assertEquals(1, safe.getKeys(TestRecord::class.java).size)

        testRecord.value = testValue2
        safe.update(testRecord)
        Assert.assertEquals(testValue2, safe.get(testKey, TestRecord::class.java)!!.value)

        safe.remove(testRecord)
        Assert.assertNull(safe.get(testKey, TestRecord::class.java))
    }


}