package org.kui.rule

import org.kui.security.model.IfThenRule
import java.text.ParseException
import java.util.regex.Pattern

fun parseIfThenRule(rule: String) : IfThenRule {

    val ifThenMatcher = Pattern.compile("if\\s*\\((?<criteria>.*)\\)\\s*then (?<action>.*)").matcher(rule)

    if (!ifThenMatcher.matches()) {
        throw ParseException("If then clause not found in $rule.", 0)
    }

    val criteria = ifThenMatcher.group("criteria").trim()

    val criteriaMatchers = Pattern.compile("(?<method>[a-zA-Z]*)\\((?<parameters>.*)\\)").matcher(criteria)
    if (!criteriaMatchers.matches()) {
        throw ParseException("Method and parameters not found in $criteria.", 0)
    }

    val criteriaMethod = criteriaMatchers.group("method").trim()

    val criteriaParameterString = criteriaMatchers.group("parameters")

    val criteriaParameterMap = getParameters(criteriaParameterString)

    val action = ifThenMatcher.group("action").trim()

    val actionMatcher = Pattern.compile("(?<method>[a-zA-Z]*)\\((?<parameters>.*)\\)").matcher(action)

    if (!actionMatcher.matches()) {
        throw ParseException("Method and parameters not found in $action.", 0)
    }

    val actionMethod = actionMatcher.group("method").trim()

    val actionParameterString = actionMatcher.group("parameters")

    val actionParameterMap = getParameters(actionParameterString)

    return IfThenRule(criteriaMethod,criteriaParameterMap,actionMethod,actionParameterMap)

}

private fun getParameters(parametersString: String): Map<String, Any> {
    if (parametersString.trim().length == 0) {
        return mapOf()
    }
    val parameterMatchers = Pattern.compile("(?<name>[^=,]*)=(?<value>'(?:\\\\.|[^'\\\\]+)*'|[^,']*)").matcher(parametersString)
    val parameterMap = mutableMapOf<String, Any>()
    while (parameterMatchers.find()) {
        val name = parameterMatchers.group("name").trim()
        val value = parameterMatchers.group("value").trim()
        if (value.startsWith("'") && value.endsWith("'")) {
            parameterMap.put(name, value.substring(1, value.length - 1))
        } else {
            parameterMap.put(name, value.toLong())
        }
    }
    return parameterMap
}