package org.kui.storage

import org.kui.storage.model.TimeValue
import org.kui.storage.model.TimeValueCountResult
import org.kui.storage.model.TimeValueResult
import java.util.*

interface TimeValueTable {
    fun insert(container: String, key: String, timeValues: List<TimeValue>)
    fun select(beginTime: Date, beginId: UUID?, endTime_: Date, containers: List<String>, keys: List<String>) : TimeValueResult
    fun selectCount(beginTime: Date, endTime_: Date, containers: List<String>, keys: List<String>) : TimeValueCountResult
}