package org.kui.log

import org.kui.storage.TimeValueDao

val environmentLogsDao = TimeValueDao("environment_logs")
val hostLogsDao = TimeValueDao("host_logs")

