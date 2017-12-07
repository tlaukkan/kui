package org.kui.security

import org.apache.log4j.xml.DOMConfigurator
import org.junit.Assert
import org.junit.Ignore
import org.junit.Test

class SecurityTest {

    @Test
    @Ignore
    fun testSecurity() {
        DOMConfigurator.configure("log4j.xml")

        Assert.assertEquals("admin", userManagement.getUserGroups(USER_SYSTEM_USER)[0])
        Assert.assertEquals("system", userManagement.getUserGroups(USER_SYSTEM_USER)[1])

    }

}