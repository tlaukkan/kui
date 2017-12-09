package org.kui.server.worker

import org.slf4j.LoggerFactory
import org.kui.security.GROUP_ADMIN
import org.kui.security.GROUP_SYSTEM
import org.kui.security.USER_SYSTEM_USER
import org.kui.security.ContextService
import org.kui.security.model.SecurityContext
import org.kui.util.getProperty
import java.util.*
import kotlin.concurrent.thread

abstract class AbstractWorker(val workIntervalMillis: Long = 60000) {

    private val log = LoggerFactory.getLogger(AbstractWorker::class.java.name)

    val host = getProperty("work", "host")

    open fun start() : AbstractWorker {
        thread(true, true, null, this::class.java.simpleName, -1, {
            try {
                ContextService.setThreadContext(SecurityContext(USER_SYSTEM_USER, listOf(GROUP_SYSTEM, GROUP_ADMIN), ByteArray(0), Date()))
                log.info("${this::class.java.simpleName} started.")
                while (true) {
                    try {
                        checkForAssignedWorkUnits()
                    } catch (e: Exception) {
                        log.error("${this::class.java.simpleName} encountered unexpected error.", e)
                    }
                    try {
                        Thread.sleep(workIntervalMillis)
                    } catch (e: InterruptedException) {
                    }
                }
            } finally {
                log.info("${this::class.java.simpleName} exited.")
            }
        })
        return this
    }

    abstract protected fun checkForAssignedWorkUnits()

}