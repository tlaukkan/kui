package org.kui.server.api.safe

import org.kui.server.api.getApiObjectMapper
import org.kui.security.*
import org.kui.security.model.Record
import org.kui.server.api.getRecordClass
import org.kui.server.rest.StreamRestProcessor
import java.io.InputStream
import java.io.OutputStream
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties

class PutRecord : StreamRestProcessor("/api/safe/<type>/<key>", "PUT", listOf(GROUP_USER)) {
    override fun process(ids: Map<String, String>, parameters: Map<String, String>, inputStream: InputStream, outputStream: OutputStream) {
        val key = ids["key"]!!
        val type = ids["type"]!!
        val clazz = getRecordClass(type)
        val record = getApiObjectMapper().readValue(inputStream, clazz)

        if (!key.equals(record.key)) {
            throw SecurityException("Updated record had key mismatch with URL and value object.")
        }

        if (!Safe.has(key, clazz.name)) {
            throw SecurityException("No such $key:${clazz.name} record.")
        }

        val existingRecord = Safe.get(key, clazz)!!

        copyRecordProperties(record, existingRecord)

        Safe.update(existingRecord)
    }

    fun copyRecordProperties(source: Record, target: Record) {
        for (member in source::class.memberProperties.filterIsInstance<KMutableProperty<*>>()) {
            if (member.name.equals("created")) {
                continue
            }
            val value = member.getter.call(source)
            member.setter.call(target, value)
        }
    }
}
