package org.kui.server.modules.log

import org.kui.api.model.Tagger
import org.kui.security.GROUP_USER
import org.kui.security.Safe
import org.kui.storage.TimeValueDao
import views.alerts.activity.ActivityAlert

object LogModule {
    val environmentLogsDao = TimeValueDao("environment_logs")
    val hostLogsDao = TimeValueDao("host_logs")
    val environmentTagsDao = TimeValueDao("environment_tags")
    val hostTypeTagsDao = TimeValueDao("host_type_tags")
    val logTagsDao = TimeValueDao("log_tags")

    fun initialize() {
        Safe.registerType(ActivityAlert::class.java)
        Safe.permitAllOperations(ActivityAlert::class.java, listOf(GROUP_USER))

        Safe.registerType(Tagger::class.java)
        Safe.permitAllOperations(Tagger::class.java, listOf(GROUP_USER))
    }
}