package org.kui.server.api.users.login

import org.kui.security.GROUP_ADMIN
import org.kui.security.GROUP_SYSTEM
import org.kui.security.GROUP_USER
import org.kui.security.userManagement
import org.kui.server.api.getApiObjectMapper
import org.kui.api.model.User
import org.kui.server.rest.StreamRestProcessor
import java.io.InputStream
import java.io.OutputStream

class PutUser : StreamRestProcessor("/api/users/<username>", "PUT", listOf(GROUP_ADMIN)) {
    override fun process(ids: Map<String, String>, parameters: Map<String, String>, inputStream: InputStream, outputStream: OutputStream) {
        val userKey = ids["username"]!!
        val user = getApiObjectMapper().readValue(inputStream, User::class.java)

        if (!userKey.equals(user.username)) {
            throw SecurityException("Add user had username mismatch with URL and value object.")
        }

        userManagement.updateUser(user.username!!, user.email!!, user.password)

        val requestedGroups = user.groups!!
        val existingGroups = userManagement.getUserGroups(user.username!!)

        for (requestedGroup in requestedGroups) {
            if (!existingGroups.contains(requestedGroup) && !requestedGroup.equals(GROUP_USER)&& !requestedGroup.equals(GROUP_SYSTEM)) {
                userManagement.grantGroup(user.username!!, requestedGroup)
            }
        }

        for (existingGroup in existingGroups) {
            if (!requestedGroups.contains(existingGroup) && !existingGroup.equals(GROUP_USER) && !existingGroup.equals(GROUP_SYSTEM)) {
                userManagement.revokeGroup(user.username!!, existingGroup)
            }
        }
    }
}
