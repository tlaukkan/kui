package org.kui.storage

import getCassandraCredentials
import org.kui.model.TimeValue
import org.kui.model.TimeValueResult
import org.kui.model.TimeValueRow
import org.kui.security.crypto
import org.kui.storage.cassandra.CassandraTimeValueTable
import java.util.*

class TimeValueDao(keyspace: String, table: String, truncate: Boolean = false) {

    val timeValueTable = CassandraTimeValueTable(getCassandraCredentials("storage"), keyspace, table, truncate)

    fun add(container: String, key: String, timeValues: List<TimeValue>) {
        val encryptedValues = mutableListOf<TimeValue>()
        for (timeValue in timeValues) {
            val meta = "$container:$key:${timeValue.time.time}"
            val nonce = meta.hashCode().toString().toByteArray()
            val aad = meta.toByteArray()
            val cipherText = crypto.encrypt(nonce, aad, timeValue.value)
            encryptedValues.add(TimeValue(timeValue.time, cipherText))
        }
        timeValueTable.insert(container, key, encryptedValues)
    }

    fun get(beginId: UUID?, beginTime: Date, endTime: Date, containers: List<String>, keys: List<String>): TimeValueResult {
        val values = arrayListOf<TimeValueRow>()
        val result = timeValueTable.select(beginId, beginTime, endTime, containers, keys)
        for (row in result.rows) {
            val meta = ("${row.container}:${row.key}:${row.time.time}")
            val nonce = meta.hashCode().toString().toByteArray()
            val aad = meta.toByteArray()
            val plainText = crypto.decrypt(nonce, aad, row.value)
            values.add(TimeValueRow(row.id, row.container, row.key, row.time, row.received, plainText))
        }
        return TimeValueResult(result.endTime, result.nextBeginId, values)
    }

    fun count(beginTime_: Date, endTime: Date, containers: List<String>, keys: List<String>): Long {
        var count = 0L
        var beginTime = beginTime_
        do {
            val result = timeValueTable.selectCount(beginTime, endTime, containers, keys)
            count += result.count
            if (result.nextBeginTime != null) {
                beginTime = result.nextBeginTime!!
            }
        } while (result.nextBeginTime != null)

        return count
    }
}