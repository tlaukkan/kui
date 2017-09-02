package org.kui.security.model

import java.util.*

data class GroupRecord(
        override var key: String? = null,
        override var created: Date? = null,
        override var modified: Date? = null
        ) : Record