package org.kui.server

import io.undertow.Undertow
import org.junit.Before
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import javax.net.ssl.*

open class ApiTest {
    var server: Undertow = configureServer()

    @Before
    fun before()  {
        disableSslVerification()
    }

    private fun disableSslVerification() {
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
}