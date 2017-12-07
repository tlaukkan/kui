package org.kui.security.model

import java.util.*

data class UserRecord(
        override var key: String? = null,
        override var created: Date? = null,
        override var modified: Date? = null,
        var email: String? = null,
        var passwordHash: ByteArray? = null,
        var passwordLoginFailed: Date? = null
        ) : Record