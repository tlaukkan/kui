package org.kui.server.storage

import org.kui.storage.TimeValueDao

val environmentLogsDao = TimeValueDao("environment_logs")
val hostLogsDao = TimeValueDao("host_logs")
val environmentTagsDao = TimeValueDao("environment_tags")
val hostTypeTagsDao = TimeValueDao("host_type_tags")
val logTagsDao = TimeValueDao("log_tags")