package org.kui.server.work

import org.slf4j.LoggerFactory
import org.kui.api.model.WorkUnit
import org.kui.security.safe
import java.util.*

abstract class AbstractCoordinatedWorker(workInternalMillis: Long) : AbstractWorker(workInternalMillis) {

    private val log = LoggerFactory.getLogger(AbstractCoordinatedWorker::class.java.name)

    override fun start() : AbstractWorker {
        super.start()
        coordinatedWorkers.put(this::class.java.simpleName, this)
        return this
    }

    abstract fun createWorkUnits()

    override fun checkForAssignedWorkUnits() {

        val assignedUnits = safe.getWithKeyPrefix("$host.${this::class.java.simpleName.toString().toLowerCase()}", WorkUnit::class.java)
        for (unit in assignedUnits) {
            if (unit.started == null) {
                unit.started = Date()
                //log.info("${unit.host!!} started working on unit ${URLDecoder.decode(unit.dataKey, "UTF-8")} of ${unit.workerClass}.")
            }

            try {
                safe.update(unit) // Update the timestamp
            } finally {
                if (work(unit)) {
                    safe.remove(unit) // Work completed
                    //log.info("${unit.host!!} completed working on unit ${URLDecoder.decode(unit.dataKey, "UTF-8")} of ${unit.workerClass}.")
                } else {
                    unit.paused = Date()
                    safe.update(unit) // Update the timestamp
                    //log.info("${unit.host!!} paused working on unit ${URLDecoder.decode(unit.dataKey, "UTF-8")} of ${unit.workerClass}.")
                }
            }

        }

    }

    abstract protected fun work(unit: WorkUnit) : Boolean


}