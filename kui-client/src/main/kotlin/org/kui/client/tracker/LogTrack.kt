package org.kui.client.tracker

import java.util.*

/**
 * Created by tlaukkan on 6/29/2017.
 */
data class LogTrack(var logPath: String = "", var created: Date = Date(), var modified: Date = Date(), var lastLineCreated: Date = Date(), var filePosition: Long = 0, var lineIndex: Long = 0)