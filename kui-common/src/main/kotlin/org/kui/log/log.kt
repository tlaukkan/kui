package org.kui.log

import org.kui.storage.TimeValueDao

val environmentLogsDao = TimeValueDao("whiteice3", "environment_logs", false)
val hostLogsDao = TimeValueDao("whiteice3", "host_logs", false)

