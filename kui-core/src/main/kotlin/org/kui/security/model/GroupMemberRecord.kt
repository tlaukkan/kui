package org.kui.security.model

import java.util.*

data class GroupMemberRecord(
        override var key: String? = null,
        override var created: Date? = null,
        override var modified: Date? = null,
        var group: String? = null,
        var user: String? = null
        ) : Record