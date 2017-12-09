package org.kui.storage.cassandra;

import com.google.common.base.Charsets;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Code copier from DataStax Cassandra driver UUIDs class to generate host part of the time based UUIDs.
 */
public class UuidClockSeqAndNodeUtil {

    public static long makeClockSeqAndNode(final String logPath) {
        long clock = new Random(System.currentTimeMillis()).nextLong();
        long node = makeNode(logPath);
        long lsb = 0;
        lsb |= (clock & 0x0000000000003FFFL) << 48;
        lsb |= 0x8000000000000000L;
        lsb |= node;
        return lsb;
    }

    private static long makeNode(final String logPath) {

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

            MessageDigest digest = MessageDigest.getInstance("MD5");
            for (String address : getAllLocalAddresses()) {
                update(digest, address);
            }

            Properties props = System.getProperties();
            update(digest, props.getProperty("java.vendor"));
            update(digest, props.getProperty("java.vendor.url"));
            update(digest, props.getProperty("java.version"));
            update(digest, props.getProperty("os.arch"));
            update(digest, props.getProperty("os.name"));
            update(digest, props.getProperty("os.version"));
            update(digest, logPath);

            byte[] hash = digest.digest();

            long node = 0;
            for (int i = 0; i < 6; i++)
                node |= (0x00000000000000ffL & (long) hash[i]) << (i * 8);
            // Since we don't use the mac address, the spec says that multicast
            // bit (least significant bit of the first byte of the node ID) must be 1.
            return node | 0x0000010000000000L;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static void update(MessageDigest digest, String value) {
        if (value != null)
            digest.update(value.getBytes(Charsets.UTF_8));
    }

    private static Set<String> getAllLocalAddresses() {
        Set<String> allIps = new HashSet<String>();
        try {
            InetAddress localhost = InetAddress.getLocalHost();
            allIps.add(localhost.toString());
            // Also return the hostname if available, it won't hurt (this does a dns lookup, it's only done once at startup)
            allIps.add(localhost.getHostName());
        } catch (UnknownHostException e) {
            // Ignore, we'll try the network interfaces anyway
        }

        try {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            if (en != null) {
                while (en.hasMoreElements()) {
                    Enumeration<InetAddress> enumIpAddr = en.nextElement().getInetAddresses();
                    while (enumIpAddr.hasMoreElements())
                        allIps.add(enumIpAddr.nextElement().toString());
                }
            }
        } catch (SocketException e) {
            // Ignore, if we've really got nothing so far, we'll throw an exception
        }

        return allIps;
    }
}
