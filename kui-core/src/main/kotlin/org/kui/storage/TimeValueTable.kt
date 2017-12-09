package org.kui.storage

import java.util.*

interface TimeValueTable {
    fun insert(container: String, key: String, timeValues: List<TimeValue>)
    fun select(beginId: UUID?, beginTime: Date, endTime_: Date, containers: List<String>, keys: List<String>) : TimeValueResult
    fun selectCount(beginTime: Date, endTime_: Date, containers: List<String>, keys: List<String>) : TimeValueCountResult
}