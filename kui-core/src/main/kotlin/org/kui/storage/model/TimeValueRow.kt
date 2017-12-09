package org.kui.storage.model

import java.util.*

data class TimeValueRow(var id: String, var container: String, var key: String, var time: Date, var received: Date, val value: ByteArray)
