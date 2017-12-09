package org.kui.server.api.users

data class PasswordChange (
        val oldPassword: String? = null,
        val newPassword: String? = null)