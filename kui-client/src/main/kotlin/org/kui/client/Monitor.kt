package org.kui.client

import org.slf4j.LoggerFactory
import java.lang.management.ManagementFactory
import java.nio.file.FileSystems
import javax.management.Attribute
import javax.management.ObjectName
import kotlin.concurrent.thread

class Monitor {

    private val log = LoggerFactory.getLogger(Monitor::class.java.name)

    fun start() : Monitor {
        Thread.sleep(3000)
        thread(true, true, null, "monitor", -1 , {
            while (true) {
                try {
                    report()
                } catch (e: Exception) {
                    log.error("Unexpected error in monitor thread.", e)
                }
                Thread.sleep(15000)
            }
        })
        return this
    }

    fun report() {
        log.info("MONITOR ${getCpuUsage()} ${getMemoryUsage()} ${getDiskUsage()}")
    }

    fun getCpuUsage() : String {
        val mbs = ManagementFactory.getPlatformMBeanServer()
        val name = ObjectName.getInstance("java.lang:type=OperatingSystem")
        val list = mbs.getAttributes(name, arrayOf("SystemCpuLoad"))
        if (list.isEmpty()) return ""
        val att = list.get(0) as Attribute
        val value  = att.getValue() as Double
        if (value == -1.0) return ""
        return "cpu=${((value * 1000) / 10.0).toInt()}%"
    }

    fun getMemoryUsage() : String {
        val operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean() as com.sun.management.OperatingSystemMXBean
        val total = operatingSystemMXBean.totalPhysicalMemorySize
        val free = operatingSystemMXBean.freePhysicalMemorySize
        return "mem=${(100.0 * (total - free) / total).toInt()}%"
    }

    fun getDiskUsage() : String {
        val builder = StringBuilder()
        for (store in FileSystems.getDefault().fileStores) {
            if (builder.length > 0) {
                builder.append(" ")
            }
            val total = store.totalSpace
            val used = (store.totalSpace - store.unallocatedSpace)
            builder.append("disk=${(100.0 * used / total).toInt()}% $store")
        }
        return builder.toString()
    }

}