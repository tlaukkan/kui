package org.kui.server.api

import com.fasterxml.jackson.databind.ObjectMapper
import org.kui.security.Safe
import org.kui.security.model.HostRecord
import org.kui.security.model.Record

fun getApiObjectMapper() : ObjectMapper {
    return ObjectMapper()
}

fun getRecordClass(type: String) : Class<Record> {
    if (type.equals("host")) {
        return HostRecord::class.java as Class<Record>
    } else {
        return Safe.getClass(type) as Class<Record>
    }
}