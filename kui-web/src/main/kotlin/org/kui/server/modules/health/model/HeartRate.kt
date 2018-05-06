package org.kui.server.modules.health.model

import java.util.*

data class HeartRate (
        var time: Date? = null,
        var hr: Int? = null,
        var rrs: IntArray? = null
)