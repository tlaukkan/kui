package org.kui.api.model

import java.util.*

data class User (
        var username: String? = null,
        var created: Date? = null,
        var modified: Date? = null,
        val email: String? = null,
        val password: String? = null,
        val groups: Array<String>? = null)