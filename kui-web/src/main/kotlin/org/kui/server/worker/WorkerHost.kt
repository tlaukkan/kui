package org.kui.server.worker

import org.kui.security.model.Record
import java.util.*

data class WorkerHost(
        override var key: String? = null,
        override var created: Date? = null,
        override var modified: Date? = null) : Record