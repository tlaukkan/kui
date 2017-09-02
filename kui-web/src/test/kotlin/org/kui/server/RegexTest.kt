package org.kui.server

import org.junit.Assert
import org.junit.Test

class RegexTest {

    @Test
    fun testAllowedCharacters() {

        val allowedCharactersRegEx = Regex("^[a-zA-Z0-9/]+$")
        Assert.assertTrue("abcdABCD/".matches(allowedCharactersRegEx))
        Assert.assertFalse("".matches(allowedCharactersRegEx))
        Assert.assertFalse("_".matches(allowedCharactersRegEx))

    }

}