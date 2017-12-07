package org.kui.security.model

import java.util.*

data class HostRecord(
        override var key: String? = null,
        override var created: Date? = null,
        override var modified: Date? = null,
        var environment: String? = null,
        var environmentType: String? = null,
        var hostType: String? = null,
        var owner: String? = null
        ) : Record