package org.kui.tag

import org.kui.storage.TimeValueDao

val environmentTagsDao = TimeValueDao("environment_tags")

val hostTypeTagsDao = TimeValueDao("host_type_tags")

val logTagsDao = TimeValueDao("log_tags")

