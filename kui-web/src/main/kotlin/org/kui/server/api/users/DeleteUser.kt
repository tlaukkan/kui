package org.kui.server.api.users.login

import org.kui.security.GROUP_ADMIN
import org.kui.security.userManagement
import org.kui.server.rest.StreamRestProcessor
import java.io.InputStream
import java.io.OutputStream

class DeleteUser : StreamRestProcessor("/api/users/<username>", "DELETE", listOf(GROUP_ADMIN)) {
    override fun process(ids: Map<String, String>, parameters: Map<String, String>, inputStream: InputStream, outputStream: OutputStream) {
        val userKey = ids["username"]!!
        userManagement.removeUser(userKey)
    }
}
