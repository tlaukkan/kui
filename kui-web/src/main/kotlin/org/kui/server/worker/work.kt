package org.kui.server.worker

import org.kui.security.Safe
import org.kui.server.worker.workers.ActivityAlertWorker

val coordinatedWorkers = mutableMapOf<String, AbstractCoordinatedWorker>()

fun addWorkUnit(dataKey: String?, workerClass: Class<ActivityAlertWorker>) {
    val newUnit = WorkUnit()
    newUnit.dataKey = dataKey
    newUnit.workerClass = workerClass.simpleName
    newUnit.key = "${newUnit.workerClass.toString().toLowerCase()}.${newUnit.dataKey}"
    Safe.add(newUnit)
}
