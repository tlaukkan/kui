package org.kui.storage.cassandra

import com.datastax.driver.core.*
import org.kui.storage.*
import org.kui.storage.TimeUuidGenerator
import org.kui.storage.TimeValueTable
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

open class CassandraTimeValueTable(table: String, truncate: Boolean = false) : CassandraTable(), TimeValueTable {

    val logTimeUuidGenerators = mutableMapOf<String, TimeUuidGenerator>()

    val insert: PreparedStatement

    val select: PreparedStatement

    val selectCount: PreparedStatement

    init {
        if (truncate) {
            session.execute("DROP TABLE IF EXISTS $table;")
        }

        session.execute("CREATE TABLE IF NOT EXISTS $table(ym varchar, id timeuuid, r timestamp, c timestamp, v blob, k text, co text, PRIMARY KEY((ym, co, k), id));")

        insert = session.prepare("INSERT INTO $table (ym, id, r, c, v, k, co) VALUES (:ym, :id, :r, :c, :v, :k, :co)")

        select = session.prepare("SELECT * FROM $table WHERE ym = ? AND co IN ? AND k IN ? AND id > ? AND id <= ? LIMIT 1000")

        selectCount = session.prepare("SELECT COUNT(*) AS c FROM $table WHERE ym = ? AND co IN ? AND k IN ? AND id > ? AND id <= ?")
    }

    override fun insert(container: String, key: String, timeValues: List<TimeValue>) {
        val fullSeriesName = "$container:$key"
        if (!logTimeUuidGenerators.containsKey(fullSeriesName)) {
            logTimeUuidGenerators[fullSeriesName] = TimeUuidGenerator(fullSeriesName, true)
        }
        val uuidGenerator = logTimeUuidGenerators[fullSeriesName]!!

        val simpleDateFormat = SimpleDateFormat("yyMM")
        simpleDateFormat.timeZone = TimeZone.getTimeZone("UTC")

        var batchStatement = BatchStatement()
        var n = 0
        for (timeValue in timeValues) {
            val yymm = simpleDateFormat.format(timeValue.time)
            val id = uuidGenerator.generate(timeValue.time!!.time / 1000)
            batchStatement.add(insert.bind(yymm, id,  Date (), timeValue.time, ByteBuffer.wrap(timeValue.value), key, container))

            n++
            if (n > 50) {
                session.execute(batchStatement)
                batchStatement = BatchStatement()
                n=0
            }
        }
        if (n > 0) {
            session.execute(batchStatement)
        }
    }

    override fun select(beginId: UUID?, beginTime: Date, endTime_: Date, containers: List<String>, key: List<String>) : TimeValueResult {

        val endTime: Date
        val endTimeChanged: Boolean
        if (beginTime.month != endTime_.month) {
            endTime = getUtcEndOfMonth(beginTime)
            endTimeChanged = true
        } else {
            endTime = endTime_
            endTimeChanged = false
        }

        val simpleDateFormat = SimpleDateFormat("yyMM")
        simpleDateFormat.timeZone = TimeZone.getTimeZone("UTC")

        val yymm = simpleDateFormat.format(beginTime)
        val beginTimeId: UUID
        if (beginId != null) {
            beginTimeId = beginId
        } else {
            beginTimeId = generateTimedUuidForComparison(beginTime!!.time / 1000)
        }
        val endTimeId = generateTimedUuidForComparison(endTime!!.time / 1000 + 1)

        val boundStatement = select.bind(yymm, containers, key, beginTimeId, endTimeId)

        var resultEndTime: Date? = null
        var nextBeginId: String? = null
        val rows = mutableListOf<TimeValueRow>()

        for (row in session.execute(boundStatement).all()) {
            rows.add(TimeValueRow(row.getUUID("id").toString(), row.getString("co"), row.getString("k"), row.getTimestamp("c"), row.getTimestamp("r"), row.getBytes("v").array()))
        }

        if (rows.size == 1000) {
            resultEndTime = rows.last().time
            nextBeginId = rows.last().id
        } else if (endTimeChanged) {
            resultEndTime = endTime
            nextBeginId = endTimeId.toString()
        }

        return TimeValueResult(resultEndTime, nextBeginId, rows)
    }

    override fun selectCount(beginTime: Date, endTime_: Date, containers: List<String>, key: List<String>) : TimeValueCountResult {

        val endTime: Date
        val endTimeChanged: Boolean
        if (beginTime.month != endTime_.month) {
            endTime = getUtcEndOfMonth(beginTime)
            endTimeChanged = true
        } else {
            endTime = endTime_
            endTimeChanged = false
        }

        val simpleDateFormat = SimpleDateFormat("yyMM")
        simpleDateFormat.timeZone = TimeZone.getTimeZone("UTC")

        val yymm = simpleDateFormat.format(beginTime)
        val beginTimeId = generateTimedUuidForComparison(beginTime!!.time / 1000)
        val endTimeId = generateTimedUuidForComparison(endTime!!.time / 1000 + 1)

        val boundStatement = selectCount.bind(yymm, containers, key, beginTimeId, endTimeId)

        val count = session.execute(boundStatement).single().getLong("c")

        if (endTimeChanged) {
            return TimeValueCountResult(endTime, count)
        } else {
            return TimeValueCountResult(null, count)
        }
    }

    private fun getUtcEndOfMonth(beginTime: Date): Date {
        val utcZoneId = ZoneId.of("UTC")
        val beginZonedDateTime = beginTime.toInstant().atZone(utcZoneId)
        val year = beginZonedDateTime.year
        val month = beginZonedDateTime.monthValue
        val dayOfMonth = beginZonedDateTime.dayOfMonth
        val beginLocalDate = LocalDate.of(year, month, dayOfMonth)
        val endZonedTime = ZonedDateTime.of(year, month, beginLocalDate.lengthOfMonth(), 23, 59, 59, 999, utcZoneId)
        val endOfMonthDate = Date.from(endZonedTime.toInstant())
        return endOfMonthDate
    }

}