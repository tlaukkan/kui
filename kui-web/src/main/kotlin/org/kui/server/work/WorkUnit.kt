package org.kui.api.model

import org.kui.security.model.Record
import java.util.*

data class WorkUnit(
        override var key: String? = null,
        override var created: Date? = null,
        override var modified: Date? = null,
        var started: Date? = null,
        var paused: Date? = null,
        var dataKey: String? = null,
        var workerClass: String? = null,
        var host: String? = null) : Record