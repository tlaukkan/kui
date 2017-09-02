package org.kui.storage

import org.kui.model.TimeValue
import org.kui.model.TimeValueCountResult
import org.kui.model.TimeValueResult
import java.util.*

interface TimeValueTable {
    fun insert(container: String, key: String, timeValues: List<TimeValue>)
    fun select(beginId: UUID?, beginTime: Date, endTime_: Date, containers: List<String>, key: List<String>) : TimeValueResult
    fun selectCount(beginTime: Date, endTime_: Date, containers: List<String>, key: List<String>) : TimeValueCountResult
}