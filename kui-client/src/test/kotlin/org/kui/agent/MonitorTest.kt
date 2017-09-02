package org.kui.agent

import org.apache.commons.codec.binary.Hex
import org.junit.Ignore
import org.junit.Test
import java.io.File
import java.io.FileInputStream
import java.nio.file.FileSystems
import java.lang.management.ManagementFactory
import java.security.MessageDigest
import javax.management.ObjectName
import javax.management.Attribute

class MonitorTest {

    @Test
    @Ignore
    fun testFileSystemRead() {
        for (store in FileSystems.getDefault().fileStores) {
            val total = store.totalSpace
            val used = (store.totalSpace - store.unallocatedSpace)
            println("Used: ${store.toString()}  ${100 * used / total}")
        }

        for (file in File.listRoots()) {
            processDirectory(file)
        }
    }

    private fun processDirectory(file: File) {
        println(file)
        if (file.list() != null) {
            for (child in file.list()) {
                val childFile = File(file.absolutePath + File.separator + child)

                if (childFile.isFile) {
                    var md5: String
                    try {
                        md5 = md5(childFile)
                    } catch (e: Exception) {
                        md5 = "?"
                    }
                    println(childFile.path + " " + md5)
                }

                if (childFile.isDirectory) {
                    processDirectory(childFile)
                }
            }
        }
    }

    fun md5(file: File): String {
        val inputStream = FileInputStream(file)
        val bytes = ByteArray(1024)
        val messageDigest = MessageDigest.getInstance("MD5")
        var bytesRead: Int
        do {
            bytesRead = inputStream.read(bytes)
            if (bytesRead > 0) {
                messageDigest.update(bytes, 0, bytesRead)
            }
        } while (bytesRead != -1)
        inputStream.close()
        return String(Hex.encodeHex(messageDigest.digest()))
    }

    @Test
    @Ignore
    fun testMemoryRead() {
        val operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean() as com.sun.management.OperatingSystemMXBean
        val total = operatingSystemMXBean.totalPhysicalMemorySize
        val free = operatingSystemMXBean.freePhysicalMemorySize
        println("Used: ${100 * (total - free) / total}%")
    }

    @Test
    @Ignore
    fun testCpuRead() {
        while(true) {
            println("Used: ${getCpuLoad()}%")
            Thread.sleep(1000)
        }
    }

    fun getCpuLoad() : Double {
        val mbs = ManagementFactory.getPlatformMBeanServer()
        val name = ObjectName.getInstance("java.lang:type=OperatingSystem")
        val list = mbs.getAttributes(name, arrayOf("SystemCpuLoad"))

        if (list.isEmpty()) return Double.NaN

        val att = list.get(0) as Attribute
        val value  = att.getValue() as Double

        if (value == -1.0) return Double.NaN

        return (value * 1000) / 10.0
    }



}
