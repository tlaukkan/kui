package org.kui.server.api.users

import org.kui.model.LogLine

data class LogBatch (
        var environment: String? = null,
        var environmentType: String? = null,
        var host: String? = null,
        var hostType: String? = null,
        var log: String? = null,
        val lines: Array<LogLine>? = null)