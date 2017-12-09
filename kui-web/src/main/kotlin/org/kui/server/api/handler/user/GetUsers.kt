package org.kui.server.api.users

import org.kui.api.model.User
import org.kui.security.GROUP_ADMIN
import org.kui.security.UserManagement
import org.kui.server.api.getApiObjectMapper
import org.kui.server.StreamRestProcessor
import java.io.InputStream
import java.io.OutputStream
import java.util.stream.Collectors

class GetUsers : StreamRestProcessor("/api/users", "GET", listOf(GROUP_ADMIN)) {
    override fun process(ids: Map<String, String>, parameters: Map<String, String>, inputStream: InputStream, outputStream: OutputStream) {
        getApiObjectMapper().writeValue(outputStream, UserManagement.getUsers().stream()
                .map({ user -> User(user.key, user.created, user.modified, user.email, null, UserManagement.getUserGroups(user.key!!).toTypedArray()) })
                .collect(Collectors.toList()))
    }
}
