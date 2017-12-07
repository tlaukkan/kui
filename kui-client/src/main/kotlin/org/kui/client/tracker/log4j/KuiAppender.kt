package org.kui.client.tracker.log4j

import org.kui.client.disableSslVerification
import org.apache.log4j.AppenderSkeleton
import org.apache.log4j.spi.LoggingEvent
import org.kui.client.tracker.LogStorageClient
import org.kui.model.LogLine
import org.kui.util.getProperty
import org.kui.util.setProperty
import java.util.*
import kotlin.concurrent.thread


class KuiAppender : AppenderSkeleton() {

    var events = LinkedList<LoggingEvent>()

    var logStorageClient: LogStorageClient? = null

    init {
        if (System.getenv("HOSTNAME") != null) {
            println("Set client host name according to environment variables: ${System.getenv("HOSTNAME")}")
            setProperty("client", "host", System.getenv("HOSTNAME"))
        }

        if (getProperty("client", "log.storage.api.url").contains("127.0.0.1")) {
            disableSslVerification()
        }

        thread(true, true, null, "kui-appender-thread", -1, {
            sendLoop()
        })
    }

    fun sendLoop() {

        val log = getProperty("client", "appender.log")
        while (true) {
            try {
                Thread.sleep(10000)
            } catch (e : InterruptedException) {
                
            }
            try {
                logStorageClient = LogStorageClient()
                while (true) {
                    try {
                        Thread.sleep(300)
                    } catch (e : InterruptedException) {

                    }

                    val lines = mutableListOf<LogLine>()
                    var eventsToSend: Int = 0
                    synchronized(events) {
                        if (events.size > 0) {
                            eventsToSend = Math.min(events.size, 1000)

                            for (i in 0..eventsToSend - 1) {
                                val event = events.get(i)
                                lines.add(LogLine(Date(event.timeStamp), "${event.getLevel().toString().padEnd(5, ' ')} ${event.renderedMessage} [${event.threadName}]"))

                                if (event.throwableStrRep != null) {
                                    for (throwableLine in event.throwableStrRep) {
                                        lines.add(LogLine(Date(event.timeStamp), "      ${throwableLine} [${event.threadName}]"))
                                    }
                                }

                            }
                        }
                    }

                    if (eventsToSend > 0) {
                        logStorageClient!!.insertLines(log, lines)
                    }

                    synchronized(events) {
                        if (eventsToSend > 0) {
                            for (i in 0 .. eventsToSend - 1) {
                                events.pop()
                            }
                        }
                    }

                }
            } catch (throwable: Throwable) {
                println("Error sending logs: $throwable")
                throwable.printStackTrace()
            }
        }
    }

    override fun append(event: LoggingEvent) {
        synchronized(events) {
            if (events.size > 100000) {
                events.pop()
                println("KUI appender event queue full (100000 log events). Dropping old events.")
            }
            events.add(event)
        }
    }

    override fun close() {}

    override fun requiresLayout(): Boolean {
        return false
    }

}