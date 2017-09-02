package org.kui.model

import org.kui.security.model.Tag
import java.util.*

data class LogRow(var id: String? = null, var host: String? = null,var log: String? = null,var time: Date? = null, val line: String? = null, var tags: Array<Tag>? = null)
