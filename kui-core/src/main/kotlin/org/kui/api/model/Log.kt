package org.kui.security.model

import java.util.*

data class Log(
        var host: String? = null,
        var log: String? = null,
        var created: Date? = null,
        var modified: Date? = null
        )