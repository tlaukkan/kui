package org.kui.security.model

import java.util.*

data class SecurityContext(
        val user: String,
        val groups: List<String>,
        val securityTokenHash: ByteArray,
        var lastAccess: Date
)