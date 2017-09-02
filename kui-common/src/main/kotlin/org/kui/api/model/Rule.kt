package org.kui.api.model

import org.kui.security.model.Record
import java.util.*

data class Rule (
        override var key: String? = null,
        override var created: Date? = null,
        override var modified: Date? = null,
        var rule: String? = null) : Record