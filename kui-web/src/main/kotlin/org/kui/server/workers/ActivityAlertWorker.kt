package org.kui.server.workers

import org.slf4j.LoggerFactory
import org.kui.api.model.WorkUnit
import org.kui.security.Safe
import org.kui.server.work.AbstractCoordinatedWorker
import org.kui.server.work.addWorkUnit
import org.kui.server.api.logs.environmentTagsDao
import org.kui.server.api.logs.hostTypeTagsDao
import org.kui.server.api.logs.logTagsDao
import org.kui.util.SmtpUtil
import views.alerts.activity.ActivityAlert
import java.text.SimpleDateFormat
import java.util.*

class ActivityAlertWorker : AbstractCoordinatedWorker(60000) {

    private val log = LoggerFactory.getLogger(ActivityAlertWorker::class.java.name)

    override fun createWorkUnits() {
        // Creating work units if they do not exist.
        for (activityAlert in Safe.getAll(ActivityAlert::class.java)) {
            if (!activityAlert.workUnitCreated) {
                val dataKey = activityAlert.key
                val workerClass = ActivityAlertWorker::class.java
                addWorkUnit(dataKey, workerClass)
                activityAlert.workUnitCreated = true
                Safe.update(activityAlert)
                log.info("${host!!} added work unit ${dataKey} of ${workerClass}.")
            }
        }
    }

    override fun work(unit: WorkUnit): Boolean {

        val activityAlert = Safe.get(unit.dataKey!!, ActivityAlert::class.java)

        if (activityAlert != null) {
            while (check(activityAlert)) {
                log.trace("${host!!} checked for activity alert ${activityAlert.key} until ${activityAlert.checkedUntil}.")
            }
            return false
        } else {
            return true // Data has been removed and work is completed.
        }
    }

    fun check(activityAlert: ActivityAlert) : Boolean {
        val begin: Date
        if (activityAlert.checkedSince == null) {
            begin = Date(Date().time - 2 * activityAlert.period!! * 60 * 1000)
        } else {
            begin = activityAlert.checkedUntil!!
        }
        val end = Date(begin.time + activityAlert.period!! * 60 * 1000)

        var delay = 5 * 60 * 1000 // 5 minute alert delay
        if (activityAlert.period!! == 1L) {
            delay = 1 * 15 * 1000 // 15 second alert delay for 1 minute periods
        }
        // Check only until now - delay minute to allow for logs to be received.
        if (end.time > Date().time - delay) {
            return false
        }

        check(activityAlert, begin, end)

        if (activityAlert.checkedSince == null) {
            activityAlert.checkedSince = begin
        }
        activityAlert.checkedUntil = end
        Safe.update(activityAlert)

        return true
    }

    fun check(activityAlert: ActivityAlert, begin: Date, end: Date) {
        if (activityAlert.log.isNullOrBlank() || activityAlert.tag.isNullOrBlank()) {
            return
        }
        val tag = activityAlert.tag!!
        val logKey = "${activityAlert.log!!.toLowerCase().replace(Regex("[^A-Za-z0-9]"), ".")}"

        val environment = activityAlert.environment
        val hostType = activityAlert.host
        val host = activityAlert.host

        if (!environment.isNullOrBlank()) {
            if (hostType.isNullOrBlank()) {
                val container = "$environment.$logKey"
                val count = environmentTagsDao.count(begin, end, listOf(container), listOf(tag))
                check(activityAlert, count, tag, container, begin, end)
            } else {
                val container = "$environment.$host.$logKey"
                val count = hostTypeTagsDao.count(begin, end, listOf(container), listOf(tag))
                check(activityAlert, count, tag, container, begin, end)
            }
        } else if (!host.isNullOrBlank()) {
            val container = "$host.$logKey"
            val count = logTagsDao.count(begin, end, listOf(container), listOf(tag))
            check(activityAlert, count, tag, container, begin, end)
        } else {
            return
        }

    }

    private fun check(activityAlert: ActivityAlert, count: Long, tag: String, container: String, begin: Date, end: Date) {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm")
        format.timeZone = TimeZone.getTimeZone("UTC")

        val beginString = format.format(begin)
        val endString = format.format(end)

        if (activityAlert.max != null && count > activityAlert.max!!) {
            val topic = "Alert: ${tag} at ${activityAlert.environment}${activityAlert.host} $beginString - $endString UTC"
            val message = "ALERT $beginString - $endString UTC system detected $tag activity at ${activityAlert.environment}${activityAlert.host} ${activityAlert.log} $count > ${activityAlert.max}."
            log.warn("$message, sending email to ${activityAlert.email}")
            if (!activityAlert.email.isNullOrBlank()) {
                SmtpUtil.send(activityAlert.email!!, topic, message)
            }
        }
        if (activityAlert.min != null && count < activityAlert.min!!) {
            val topic = "Inactivity: ${tag} at ${activityAlert.environment}${activityAlert.host} $beginString - $endString UTC ${activityAlert.log}"
            val message = "ALERT: $beginString - $endString UTC system detected $tag inactivity at ${activityAlert.environment}${activityAlert.host} ${activityAlert.log} $count < ${activityAlert.min}."
            log.warn("$message, sending email to ${activityAlert.email}")
            if (!activityAlert.email.isNullOrBlank()) {
                SmtpUtil.send(activityAlert.email!!, topic, message)
            }
        }
    }

}