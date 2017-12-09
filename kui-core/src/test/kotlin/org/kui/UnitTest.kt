package org.kui

import org.apache.log4j.xml.DOMConfigurator
import org.junit.Before

open class UnitTest {
    @Before
    fun before() {
        DOMConfigurator.configure("log4j.xml")
    }
}