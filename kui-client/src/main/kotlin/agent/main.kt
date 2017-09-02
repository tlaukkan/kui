package agent

import org.apache.log4j.xml.DOMConfigurator
import org.slf4j.LoggerFactory
import org.kui.agent.Agent
import org.kui.agent.Monitor
import org.kui.util.getProperty
import org.kui.util.getZoneOffsetMillis
import org.kui.util.setProperty
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.net.ssl.*

private val log = LoggerFactory.getLogger("agent.main")

fun main(args : Array<String>) {
    DOMConfigurator.configure("log4j.xml")

    log.info("Time Zone offset: " + getZoneOffsetMillis())

    val monitor = Monitor().start()

    if (System.getenv("HOSTNAME") != null) {
        println("Set agent host name according to environment variables: ${System.getenv("HOSTNAME")}")
        setProperty("agent", "host", System.getenv("HOSTNAME"))
    }

    if (getProperty("agent", "log.storage.api.url").contains("127.0.0.1")) {
        disableSslVerification()
    }

    val agent = Agent()

    Runtime.getRuntime().addShutdownHook(Thread({
        println("Shutdown at " + Date())
    }))

    while (true) {
        try {
            if (!agent.isConnected()) {
                if (!getProperty("agent","simulate").equals("true")) {
                    agent.connect()
                }
            }
            agent.track()
        } catch (e : Exception) {
            agent.disconnect()
            log.error("Error in tracking logs.", e)
            Thread.sleep(10000)
        }
    }
}

fun disableSslVerification() {
    try {
        // Create a trust manager that does not validate certificate chains
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(p0: Array<out java.security.cert.X509Certificate>?, p1: String?) {
            }

            override fun checkServerTrusted(p0: Array<out java.security.cert.X509Certificate>?, p1: String?) {
            }

            override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate>? {
                return null
            }

        })

        // Install the all-trusting trust manager
        val sc = SSLContext.getInstance("SSL")
        sc.init(null, trustAllCerts, java.security.SecureRandom())
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory())

        // Create all-trusting host name verifier
        val allHostsValid = object : HostnameVerifier {
            override fun verify(hostname: String, session: SSLSession): Boolean {
                return true
            }
        }

        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid)
    } catch (e: NoSuchAlgorithmException) {
        e.printStackTrace()
    } catch (e: KeyManagementException) {
        e.printStackTrace()
    }

}