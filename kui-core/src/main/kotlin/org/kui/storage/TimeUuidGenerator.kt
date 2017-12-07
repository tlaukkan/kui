package org.kui.storage

import org.slf4j.LoggerFactory
import java.util.*

/**
 * Code copied from JUG to fake time based UUIDs based loosely on log time and counter.
 */
class TimeUuidGenerator(val logPath: String, val warnOldLines: Boolean) {

    val log = LoggerFactory.getLogger(TimeUuidGenerator::class.java.name)

    var clockSeqAndNode = UuidClockSeqAndNodeUtil.makeClockSeqAndNode(logPath)
    var lastTimeSeconds = 0L
    var hundredNanoSecondCounter = 0L

    fun generate(timeSeconds: Long) : UUID {

        if (lastTimeSeconds > timeSeconds && warnOldLines) {
            log.warn("Timestamp of line in $logPath is older than that of previous line.")
        }
        if (lastTimeSeconds == timeSeconds)  {
            hundredNanoSecondCounter++
        } else {
            lastTimeSeconds = timeSeconds
            hundredNanoSecondCounter = 0L
        }

        return generateRaw()
    }

    private fun generateRaw(): UUID {
        val rawTimestamp = lastTimeSeconds * 10000000L + hundredNanoSecondCounter
        // Time field components are kind of shuffled, need to slice:
        val clockHi = rawTimestamp.ushr(32).toInt()
        val clockLo = rawTimestamp.toInt()
        // and dice
        var midhi = clockHi shl 16 or clockHi.ushr(16)
        // need to squeeze in type (4 MSBs in byte 6, clock hi)
        midhi = midhi and 0xF000.inv() // remove high nibble of 6th byte
        midhi = midhi or 0x1000 // type 1
        var midhiL = midhi.toLong()
        midhiL = (midhiL shl 32).ushr(32) // to get rid of sign extension
        // and reconstruct
        val l1 = clockLo.toLong() shl 32 or midhiL
        // last detail: must force 2 MSB to be '10'
        return UUID(l1, clockSeqAndNode)
    }

}