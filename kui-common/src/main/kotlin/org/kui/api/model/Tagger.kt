package org.kui.api.model

import org.kui.security.model.Record
import java.util.*

data class Tagger (
        override var key: String? = null,
        override var created: Date? = null,
        override var modified: Date? = null,
        var environment: String? = null,
        var host: String? = null,
        var log: String? = null,
        var pattern: String? = null,
        var tag: String? = null,
        var color: String? = null) : Record