package org.kui.storage.cassandra

import com.google.common.base.Charsets

import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException
import java.net.UnknownHostException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

/**
 * Code copier from DataStax Cassandra driver UUIDs class to generate host part of the time based UUIDs.
 */
object UuidClockSeqAndNodeUtil {

    fun makeClockSeqAndNode(logPath: String): Long {
        val clock = Random(System.currentTimeMillis()).nextLong()
        val node = makeNode(logPath)
        var lsb: Long = 0
        lsb = lsb or (clock and 0x0000000000003FFFL shl 48)
        lsb = lsb or unsignedLong(0x80000000, 0x00000000)
        lsb = lsb or node
        return lsb
    }

    private fun unsignedLong(mostSignificantBits: Long, leastSignificantBits: Long) =
            (mostSignificantBits shl 32) or leastSignificantBits

    private fun makeNode(logPath: String): Long {

        /*
         * We don't have access to the MAC address (in pure JAVA at least) but
         * need to generate a node part that identify this host as uniquely as
         * possible.
         * The spec says that one option is to take as many source that
         * identify this node as possible and hash them together. That's what
         * we do here by gathering all the ip of this host as well as a few
         * other sources.
         */
        try {

            val digest = MessageDigest.getInstance("MD5")
            for (address in allLocalAddresses) {
                update(digest, address)
            }

            val props = System.getProperties()
            update(digest, props.getProperty("java.vendor"))
            update(digest, props.getProperty("java.vendor.url"))
            update(digest, props.getProperty("java.version"))
            update(digest, props.getProperty("os.arch"))
            update(digest, props.getProperty("os.name"))
            update(digest, props.getProperty("os.version"))
            update(digest, logPath)

            val hash = digest.digest()

            var node: Long = 0
            for (i in 0..5)
                node = node or (0x00000000000000ffL and hash[i].toLong() shl i * 8)
            // Since we don't use the mac address, the spec says that multicast
            // bit (least significant bit of the first byte of the node ID) must be 1.
            return node or 0x0000010000000000L
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        }

    }

    private fun update(digest: MessageDigest, value: String?) {
        if (value != null)
            digest.update(value.toByteArray(Charsets.UTF_8))
    }

    private // Also return the hostname if available, it won't hurt (this does a dns lookup, it's only done once at startup)
            // Ignore, we'll try the network interfaces anyway
            // Ignore, if we've really got nothing so far, we'll throw an exception
    val allLocalAddresses: Set<String>
        get() {
            val allIps = HashSet<String>()
            try {
                val localhost = InetAddress.getLocalHost()
                allIps.add(localhost.toString())
                allIps.add(localhost.hostName)
            } catch (e: UnknownHostException) {
            }

            try {
                val en = NetworkInterface.getNetworkInterfaces()
                if (en != null) {
                    while (en.hasMoreElements()) {
                        val enumIpAddr = en.nextElement().inetAddresses
                        while (enumIpAddr.hasMoreElements())
                            allIps.add(enumIpAddr.nextElement().toString())
                    }
                }
            } catch (e: SocketException) {
            }

            return allIps
        }
}
