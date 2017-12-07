package org.kui.server.api

import com.fasterxml.jackson.databind.ObjectMapper
import org.kui.api.model.Tagger
import org.kui.security.model.HostRecord
import org.kui.security.model.Record
import views.alerts.activity.ActivityAlert

fun getApiObjectMapper() : ObjectMapper {
    val mapper = ObjectMapper()
    return mapper
}

fun getRecordClass(type: String) : Class<Record> {
    if (type.equals("tagger")) {
        return Tagger::class.java as Class<Record>
    } else if (type.equals("activityalert")) {
        return ActivityAlert::class.java as Class<Record>
    } else if (type.equals("host")) {
        return HostRecord::class.java as Class<Record>
    } else {
        throw SecurityException("No such safe record type: $type")
    }
}