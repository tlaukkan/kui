package org.kui.server.api.users

data class User (
        var username: String? = null,
        var email: String? = null,
        var password: String? = null,
        val groups: Array<String>? = null,
        var created: Long? = null,
        var modified: Long? = null)