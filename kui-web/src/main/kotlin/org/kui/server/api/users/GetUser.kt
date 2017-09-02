package org.kui.server.api.users

import org.kui.api.model.User
import org.kui.security.GROUP_ADMIN
import org.kui.security.userManagement
import org.kui.server.api.getApiObjectMapper
import org.kui.server.rest.StreamRestProcessor
import java.io.InputStream
import java.io.OutputStream

class GetUser : StreamRestProcessor("/api/users/<username>", "GET", listOf(GROUP_ADMIN)) {
    override fun process(ids: Map<String, String>, parameters: Map<String, String>, inputStream: InputStream, outputStream: OutputStream) {
        val userKey = ids["username"]!!
        val userRecord = userManagement.getUser(userKey)!!
        val user = User(userRecord.key, userRecord.created, userRecord.modified, userRecord.email, null, userManagement.getUserGroups(userKey).toTypedArray())
        getApiObjectMapper().writeValue(outputStream, user)
    }
}
