package org.kui.util

import org.apache.commons.lang.StringUtils
import org.slf4j.LoggerFactory

import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart
import java.util.Date

object SmtpUtil {

    private val log = LoggerFactory.getLogger("org.kui.util.email")

    /**
     * Sends email.
     *
     * @param to target email addresses
     * @param subject the email subject
     * @param body the email body
     */
    fun send(to: String, subject: String, body: String) {
        val smtpHost = getProperty("smtp", "smtp-host")
        val smtpPort = getProperty("smtp", "smtp-port")
        val smtpUser = getProperty("smtp", "smtp-user")
        val smtpPassword = getProperty("smtp", "smtp-password")
        val from = getProperty("smtp", "smtp-from")
        send(smtpHost, smtpPort, smtpUser, smtpPassword, listOf(to), from, subject, body)
    }

    /**
     * Sends email.
     *
     * @param smtpHost the SMTP host
     * @param smtpPort the SMTP host port
     * @param smtpUser the SMTP user
     * @param smtpPassword the SMTP user password
     * @param to target email addresses
     * @param from from email address
     * @param subject the email subject
     * @param body the email body
     */
    fun send(smtpHost: String, smtpPort: String,
             smtpUser: String, smtpPassword: String,
             to: List<String>, from: String, subject: String, body: String) {
        try {
            val properties = System.getProperties()
            properties.put("mail.smtp.host", smtpHost)

            if (!StringUtils.isEmpty(smtpPort)) {
                properties.put("mail.smtp.port", smtpPort)
            }

            val session: Session
            if (StringUtils.isEmpty(smtpUser) || StringUtils.isEmpty(smtpPassword)) {
                session = Session.getDefaultInstance(properties, null)
                log.info("Sending unauthenticated plain text transmission of email via "
                        + smtpHost + ": " + smtpPort + " from address: " + from)
            } else {
                properties.put("mail.smtp.auth", "true")
                properties.put("mail.smtp.starttls.enable", "true")
                session = Session.getInstance(properties,
                        object : javax.mail.Authenticator() {
                            override fun getPasswordAuthentication(): PasswordAuthentication {
                                return PasswordAuthentication(smtpUser, smtpPassword)
                            }
                        })
                log.info("Sending authenticated TLS encrypted transmission of email via "
                        + smtpHost + ": " + smtpPort + " from address: " + from)
            }

            // Text part
            val textPart = MimeBodyPart()
            textPart.setHeader("MIME-Version", "1.0")
            textPart.setHeader("Content-Type", textPart.getContentType())
            textPart.setContent(body, "text/plain")

            // HTML part
            val htmlPart = MimeBodyPart()
            htmlPart.setHeader("MIME-Version", "1.0")
            htmlPart.setHeader("Content-Type", htmlPart.getContentType())

            var htmlContent = "<html><head><title>$subject</title></head><body><p><pre>$body</pre></p></body></html>"
            htmlPart.setContent(htmlContent, "text/html")

            val multiPartContent = MimeMultipart("alternative")
            multiPartContent.addBodyPart(textPart)
            multiPartContent.addBodyPart(htmlPart)

            val message = MimeMessage(session)
            message.setHeader("MIME-Version", "1.0")
            message.setHeader("Content-Type", multiPartContent.getContentType())
            message.setHeader("X-Mailer", "KUI")
            message.setSentDate(Date())
            message.setFrom(InternetAddress(from))

            if (to.size == 1) {
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to[0], false))
            } else {
                val recipientAddresses = arrayOfNulls<InternetAddress>(to.size)
                for (i in to.indices) {
                    val parsedAddress = InternetAddress.parse(to[i], false)
                    if (parsedAddress.size == 1) {
                        recipientAddresses[i] = parsedAddress[0]
                    }
                }
                message.setRecipients(Message.RecipientType.BCC, recipientAddresses)
            }

            message.setSubject(subject)
            message.setContent(multiPartContent)

            Transport.send(message)
        } catch (t: Throwable) {
            throw RuntimeException("Email sending failed.", t)
        }
    }

}