package org.kui.server.api.users.login

import org.kui.security.*
import org.kui.server.api.getApiObjectMapper
import org.kui.server.api.users.PasswordChange
import org.kui.server.rest.StreamRestProcessor
import java.io.InputStream
import java.io.OutputStream

class PutPasswordChange : StreamRestProcessor("/api/user/password", "PUT", listOf(GROUP_USER)) {
    override fun process(ids: Map<String, String>, parameters: Map<String, String>, inputStream: InputStream, outputStream: OutputStream) {
        val passwordChange = getApiObjectMapper().readValue(inputStream, PasswordChange::class.java)
        UserManagement.changeOwnPassword(passwordChange.oldPassword!!, passwordChange.newPassword!!)
    }
}
