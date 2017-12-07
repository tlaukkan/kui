package org.kui.security

import org.kui.security.model.Record
import java.util.*

data class TestRecord(override var key: String? = null,
                      var value: String = "",
                      override var created: Date? = null,
                      override var modified: Date? = null) : Record