package org.kui.server.api.users.login

import org.slf4j.LoggerFactory
import org.kui.security.GROUP_ANONYMOUS
import org.kui.security.GROUP_USER
import org.kui.security.ContextService
import org.kui.security.Crypto
import org.kui.server.api.getApiObjectMapper
import org.kui.server.StreamRestProcessor
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class DeleteSecurityContext : StreamRestProcessor("/api/security/context", "DELETE", listOf(GROUP_ANONYMOUS, GROUP_USER)) {
    private val log = LoggerFactory.getLogger(DeleteSecurityContext::class.java.name)

    override fun process(ids: Map<String, String>, parameters: Map<String, String>, inputStream: InputStream, outputStream: OutputStream) {
        val securityToken = getApiObjectMapper().readValue(inputStream, String::class.java)
        val securityTokenHash = Crypto.securityTokenHash(securityToken)
        val securityTokenHashString = Base64.getEncoder().encodeToString(securityTokenHash)

        ContextService.destroyContext(securityTokenHashString)

        log.info("User sign out: ${ContextService.getThreadContext().user}")
    }
}
