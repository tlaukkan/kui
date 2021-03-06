package org.kui.storage

import org.kui.security.Crypto
import org.kui.storage.cassandra.CassandraTimeValueTable
import org.kui.storage.dynamodb.DynamoDbTimeValueTable
import org.kui.storage.jpa.JpaTimeValueTable
import org.kui.storage.model.TimeValue
import org.kui.storage.model.TimeValueResult
import org.kui.storage.model.TimeValueRow
import org.kui.util.getProperty
import java.util.*

class TimeValueDao(table: String) {

    val timeValueTable : TimeValueTable =
            if ("cassandra".equals(getProperty("storage", "storage.type"))) {
                CassandraTimeValueTable(table)
            } else if ("dynamodb".equals(getProperty("storage", "storage.type"))) {
                DynamoDbTimeValueTable(table)
            } else {
                JpaTimeValueTable(table)
            }

    fun add(container: String, key: String, timeValues: List<TimeValue>) {
        val encryptedValues = mutableListOf<TimeValue>()
        for (timeValue in timeValues) {
            val meta = "$container:$key:${timeValue.time.time}"
            val nonce = meta.hashCode().toString().toByteArray()
            val aad = meta.toByteArray()
            val cipherText = Crypto.encrypt(nonce, aad, timeValue.value)
            encryptedValues.add(TimeValue(timeValue.time, cipherText))
        }
        timeValueTable.insert(container, key, encryptedValues)
    }

    fun get(beginTime: Date, beginId: UUID?, endTime: Date, containers: List<String>, keys: List<String>): TimeValueResult {
        val values = arrayListOf<TimeValueRow>()
        val result = timeValueTable.select(beginTime, beginId, endTime, containers, keys)
        for (row in result.rows) {
            val meta = ("${row.container}:${row.key}:${row.time.time}")
            val nonce = meta.hashCode().toString().toByteArray()
            val aad = meta.toByteArray()
            val plainText = Crypto.decrypt(nonce, aad, row.value)
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