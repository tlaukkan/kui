package org.kui.util

import org.apache.log4j.xml.DOMConfigurator
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.jvnet.mock_javamail.Mailbox
import javax.mail.internet.MimeMultipart

class SmtpUtilTest {

    @Before
    fun before() {
        DOMConfigurator.configure("log4j.xml")
    }

    @Test
    fun testSend() {
        val to = "test.email@localhost"
        val subject = "Test2"
        val body = "Test Message2"

        SmtpUtil.send(to, subject, body)

        val inbox = Mailbox.get(to)

        Assert.assertTrue(inbox.size == 1)
        Assert.assertEquals(subject, inbox[0].subject)
        Assert.assertEquals(body, (inbox[0].content as MimeMultipart).getBodyPart(0).content)
    }

}