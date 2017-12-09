package org.kui.util

import org.junit.Assert
import org.junit.Test
import org.jvnet.mock_javamail.Mailbox
import org.kui.UnitTest
import javax.mail.internet.MimeMultipart

class SmtpUtilTest : UnitTest() {

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