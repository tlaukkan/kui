package org.kui.server.api

import com.fasterxml.jackson.databind.ObjectMapper

fun getApiObjectMapper() : ObjectMapper {
    val mapper = ObjectMapper()
    return mapper
}