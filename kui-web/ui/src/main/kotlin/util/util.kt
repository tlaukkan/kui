package util

import kotlin.js.Date

/**
 * Writes given plain data object containing only primitives, arrays and other similar data objects as JSON string.
 */
fun toJson(value: Any): String {
    return JSON.stringify(value)
}

/**
 * Parses JSON string to data object containing only primitives, arrays and other similar data objects.
 */
fun <T : Any> fromJson(string: String): T {
    return JSON.parse(string)
}

/**
 * Dynamic cast to be able to cast JSON parsed objects to their type.
 */
fun <T> dynamicCast(obj: Any) : T {
    val dynamicNode: dynamic = obj
    return dynamicNode
}

external fun getDateFromMilliseconds(milliseconds: Long): Date

external fun jsonStringToDate(dateJsonString: String): Date

external fun dateStringToLongString(dateJsonString: String): String

external fun base64Encode(str: String): Date

external fun escapeHtml(str: String): String

external fun unescapeHtml(str: String): String

external fun escapeUri(message: String): String

external fun encodeURIComponent(message: String): String

external fun encodeURI(message: String): String

external fun decodeURIComponent(message: String): String

external fun showModal(id: String): Unit

external fun hideModal(id: String): Unit

fun dateToUiString(date: Date): String {
    val dynamicDate: dynamic = date
    val year = (dynamicDate.getYear() + 1900).toString().padStart(2, '0')
    val month = (dynamicDate.getMonth() + 1).toString().padStart(2, '0')
    val day = (dynamicDate.getDate()).toString().padStart(2, '0')
    val hours = (dynamicDate.getHours()).toString().padStart(2, '0')
    val minutes = (dynamicDate.getMinutes()).toString().padStart(2, '0')
    val seconds = (dynamicDate.getSeconds()).toString().padStart(2, '0')
    val milliseconds = (dynamicDate.getMilliseconds()).toString().padStart(3, '0')
    return "$year-$month-$day $hours:$minutes:$seconds.$milliseconds"
}

fun dateToShortUiString(date: Date): String {
    val dynamicDate: dynamic = date
    val year = (dynamicDate.getYear() + 1900).toString().padStart(2, '0')
    val month = (dynamicDate.getMonth() + 1).toString().padStart(2, '0')
    val day = (dynamicDate.getDate()).toString().padStart(2, '0')
    val hours = (dynamicDate.getHours()).toString().padStart(2, '0')
    val minutes = (dynamicDate.getMinutes()).toString().padStart(2, '0')
    return "$year-$month-$day $hours:$minutes"
}

fun getDateTimePartMillis(date: Date): Long {
    val dynamicDate: dynamic = date
    val hours = (dynamicDate.getHours() as Number).toLong()
    val minutes = (dynamicDate.getMinutes() as Number).toLong()
    val seconds = (dynamicDate.getSeconds() as Number).toLong()
    val milliseconds = (dynamicDate.getMilliseconds() as Number).toLong()
    return (hours * 60L * 60L * 1000L) + (minutes * 60L * 1000L) + (seconds * 1000L)  + milliseconds
}

fun getDatePart(now: Date) = getDateFromMilliseconds(now.getTime().toLong() - getDateTimePartMillis(now))

fun addMinutes(todayBegin: Date, minutes: Long) = getDateFromMilliseconds(todayBegin.getTime().toLong() + minutes * 60L * 1000L)

fun addDays(todayBegin: Date, days: Long) = getDateFromMilliseconds(todayBegin.getTime().toLong() + days * 24L * 60L * 60L * 1000L)

fun setParameter(url: String, name: String, value: String): String {
    val parts = url.split('?')
    val parameters = mutableMapOf<String, String>()
    if (parts.size == 2) {
        val nameValueStrings : List<String> = parts[1].split('&')
        for (nameValueString in nameValueStrings) {
            val nameValuePair = nameValueString.split('=')
            parameters.put(nameValuePair[0], nameValuePair[1])
        }
    }
    parameters.put(name, encodeURIComponent(value))

    val names = parameters.keys.toMutableList()
    names.sort()
    val updatedParameterString = StringBuilder()
    for (name in names) {
        if (updatedParameterString.length > 0) {
            updatedParameterString.append("&")
        }
        updatedParameterString.append(name)
        updatedParameterString.append('=')
        updatedParameterString.append(parameters[name])
    }

    return "${parts[0]}?${updatedParameterString}"
}

fun getParameters(url: String): MutableMap<String, String> {
    val parts = url.split('?')
    val parameters = mutableMapOf<String, String>()
    if (parts.size == 2) {
        val nameValueStrings : List<String> = parts[1].split('&')
        for (nameValueString in nameValueStrings) {
            val nameValuePair = nameValueString.split('=')
            parameters.put(nameValuePair[0], decodeURIComponent(nameValuePair[1]))
        }
    }
    return parameters
}

fun setToString(set: MutableSet<String>): String {
    var hostsBuilder = StringBuilder()
    for (selectedHost in set) {
        if (hostsBuilder.size > 0) {
            hostsBuilder.append(',')
        }
        hostsBuilder.append(selectedHost)
    }

    val hostsString = hostsBuilder.toString()
    return hostsString
}

fun getProperties(any: Any): MutableMap<String, Any?> {
    val objectKeys = js("Object.keys")
    val keys: dynamic = objectKeys(any)
    val obj: dynamic = any

    val map = mutableMapOf<String, Any?>()
    for (i in 0..keys.length - 1) {
        val key = keys[i]
        map.put(key, obj[key])
    }

    return map
}

fun setProperties(any: Any, values: MutableMap<String, Any?>): Unit {
    val objectKeys = js("Object.keys")
    val keys: dynamic = objectKeys(any)
    val obj: dynamic = any

    for (i in 0..keys.length - 1) {
        val key = keys[i]
        obj[key] = values[key]
    }
}

