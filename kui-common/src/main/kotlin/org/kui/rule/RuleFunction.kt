package org.kui.rule

interface RuleFunction {
    var name: String
    fun invoke(parameters: Map<String, Object>) : Unit
}