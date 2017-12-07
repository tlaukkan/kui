package org.kui.server.work

import org.slf4j.LoggerFactory
import org.kui.api.model.WorkUnit
import org.kui.api.model.WorkerHost
import org.kui.security.Safe
import java.net.URLDecoder
import java.util.*

class CoordinationWorker : AbstractWorker(2000) {

    private val log = LoggerFactory.getLogger(CoordinationWorker::class.java.name)

    var workerHost = WorkerHost(key=host)

    val random = Random()

    init {
        if (Safe.has(host, WorkerHost::class.java)) {
            workerHost = Safe.get(host, WorkerHost::class.java)!!
        } else {
            Safe.add(workerHost)
            workerHost = Safe.get(host, WorkerHost::class.java)!!
        }
    }

    override fun checkForAssignedWorkUnits() {
        if (Safe.has(host, WorkerHost::class.java)) {
            Safe.update(workerHost)
        } else {
            Safe.add(workerHost)
        }
        if (!Safe.has("coordinator", WorkUnit::class.java)) {
            Safe.add(WorkUnit(key = "coordinator", host = host, dataKey = "coordinator", workerClass = "coordinator"))
            log.info("$host self assigned as first coordinator.")
        } else {
            val unit = Safe.get("coordinator", WorkUnit::class.java)
            if (unit != null) {
                val oldCoordinatorHost = unit.host
                if (oldCoordinatorHost.equals(host)) {
                    Safe.update(unit)
                    work()
                } else {
                    if (System.currentTimeMillis() - unit.modified!!.time > workIntervalMillis * 5) {
                        unit.host = host
                        Safe.update(unit)
                        log.info("$host self assigned as coordinator due to inactivity of host $oldCoordinatorHost")
                    }
                }
            }
        }
    }

    private fun work() {
        for (coordinatedWorker in coordinatedWorkers.values) {
            coordinatedWorker.createWorkUnits()
        }

        val hosts = Safe.getAll(WorkerHost::class.java)

        val activeHosts = mutableListOf<WorkerHost>()
        for (hostCandidate in hosts) {
            if ((System.currentTimeMillis() - hostCandidate.modified!!.time) < workIntervalMillis * 5) {
                activeHosts.add(hostCandidate)
            }

            if ((System.currentTimeMillis() - hostCandidate.modified!!.time) > workIntervalMillis * 1000) {
                Safe.remove(hostCandidate)
                log.info("$host removed passive host ${hostCandidate.key} from workers.")
            }
        }

        for (unit in Safe.getAll(WorkUnit::class.java)) {
            if (!unit.key.equals("coordinator")) {
                val worker = coordinatedWorkers[unit.workerClass]!!
                if (unit.host == null || System.currentTimeMillis() - unit.modified!!.time > worker.workIntervalMillis * 1.1) {
                    val selectedWorkerHost = activeHosts.get(random.nextInt(activeHosts.size))!!
                    Safe.remove(unit)

                    unit.host = selectedWorkerHost.key
                    unit.key = "${unit.host}.${unit.workerClass.toString().toLowerCase()}.${unit.dataKey}"
                    if (!Safe.has(unit.key!!, unit.javaClass)) {
                        Safe.add(unit)
                    }
                    log.info("$host assigned ${unit.host!!} as worker for unit ${URLDecoder.decode(unit.dataKey, "UTF-8")} of ${unit.workerClass}.")
                }
            }
        }
    }

}