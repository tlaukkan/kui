package org.kui.server.api.users

import org.kui.api.model.User
import org.kui.security.GROUP_ADMIN
import org.kui.security.UserManagement
import org.kui.server.api.getApiObjectMapper
import org.kui.server.api.StreamRestProcessor
import java.io.InputStream
import java.io.OutputStream

class GetUser : StreamRestProcessor("/api/users/<username>", "GET", listOf(GROUP_ADMIN)) {
    override fun process(ids: Map<String, String>, parameters: Map<String, String>, inputStream: InputStream, outputStream: OutputStream) {
        val userKey = ids["username"]!!
        val userRecord = UserManagement.getUser(userKey)!!
        val user = User(userRecord.key, userRecord.created, userRecord.modified, userRecord.email, null, UserManagement.getUserGroups(userKey).toTypedArray())
        getApiObjectMapper().writeValue(outputStream, user)
    }
}
