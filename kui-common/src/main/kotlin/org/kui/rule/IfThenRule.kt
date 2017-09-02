package org.kui.security.model

data class IfThenRule(
        var ifFunction: String,
        var ifFunctionParameters: Map<String, Any>,
        var thenFunction: String,
        var thenFunctionParameters: Map<String, Any>
)