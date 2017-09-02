package org.kui.api.model

data class LogTagger (
        var environment: Regex? = null,
        var host: Regex? = null,
        var log: Regex? = null,
        var pattern: Regex? = null,
        var tag: String? = null,
        var color: String? = null)