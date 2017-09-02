package org.kui.security.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class Tag(
        var tag: String? = null,
        var color: String? = null
)