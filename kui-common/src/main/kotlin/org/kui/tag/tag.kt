package org.kui.tag

import org.kui.storage.TimeValueDao

val environmentTagsDao = TimeValueDao("whiteice3", "environment_tags", false)

val hostTypeTagsDao = TimeValueDao("whiteice3", "host_type_tags", false)

val logTagsDao = TimeValueDao("whiteice3", "log_tags", false)

