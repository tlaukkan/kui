package org.kui.util

import java.time.*
import java.util.*

fun getZoneOffsetMillis() : Long {
    return OffsetDateTime.now(ZoneId.systemDefault()).offset.totalSeconds * 1000L
}

/*
fun localToUtc(date: Date): Date {
    return Date(date.time - getZoneOffsetMillis())
}

fun utcToLocal(date: Date): Date {
    return Date(date.time + getZoneOffsetMillis())
}

fun utcNow() : Date {
    return localToUtc(Date())
}
*/

fun utcStartOfDay() : Date {
    val utcNow = ZonedDateTime.now(ZoneOffset.UTC)
    val utcAtStartOfDay = utcNow.toLocalDate().atStartOfDay(utcNow.zone)
    return Date.from(utcAtStartOfDay.toInstant())
}
