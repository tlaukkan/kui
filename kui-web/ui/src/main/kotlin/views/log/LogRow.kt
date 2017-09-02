package org.kui.model

import views.log.Tag

data class LogRow(var id: String?, var host: String? = null,var log: String? = null,var time: Long? = null, val line: String? = null, var tags: Array<Tag>? = null)
