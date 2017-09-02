package org.kui.security.model

import java.util.*

data class Host (
        var environmentType: String? = null,
        var environment: String? = null,
        var hostType: String? = null,
        var host: String? = null,
        var owner: String? = null,
        var created: Date? = null,
        var modified: Date? = null
        )