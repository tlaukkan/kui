package org.kui.server.api.users

import org.kui.api.model.User
import org.kui.security.*
import org.kui.security.model.UserRecord
import org.kui.server.api.getApiObjectMapper
import org.kui.server.StreamRestProcessor
import org.kui.storage.keyValueDao
import java.io.InputStream
import java.io.OutputStream

class GetSecurityContext : StreamRestProcessor("/api/security/context", "GET", listOf(GROUP_USER, GROUP_ANONYMOUS)) {
    override fun process(ids: Map<String, String>, parameters: Map<String, String>, inputStream: InputStream, outputStream: OutputStream) {
        if (ContextService.getThreadContext() == null) {
            throw SecurityException("Attempt to access current user without session.")
        }
        val userRecord = keyValueDao.get(ContextService.getThreadContext().user, UserRecord::class.java)!!
        val user = User(userRecord.key, userRecord.created, userRecord.modified, userRecord.email, null, UserManagement.getUserGroups(userRecord.key!!).toTypedArray())
        getApiObjectMapper().writeValue(outputStream, user)
    }
}
