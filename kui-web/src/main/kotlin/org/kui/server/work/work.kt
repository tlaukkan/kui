package org.kui.server.work

import org.kui.api.model.WorkUnit
import org.kui.security.Safe
import org.kui.server.workers.ActivityAlertWorker

val coordinatedWorkers = mutableMapOf<String, AbstractCoordinatedWorker>()

fun addWorkUnit(dataKey: String?, workerClass: Class<ActivityAlertWorker>) {
    val newUnit = WorkUnit()
    newUnit.dataKey = dataKey
    newUnit.workerClass = workerClass.simpleName
    newUnit.key = "${newUnit.workerClass.toString().toLowerCase()}.${newUnit.dataKey}"
    Safe.add(newUnit)
}
