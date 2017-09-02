package org.kui.security.model

import java.util.*

data class LogRecord(
        override var key: String? = null,
        override var created: Date? = null,
        override var modified: Date? = null,
        var host: String? = null,
        var log: String? = null
        ) : Record