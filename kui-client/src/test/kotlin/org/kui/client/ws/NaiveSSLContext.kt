package org.kui.client.ws

/*
 * Copyright (C) 2015 Neo Visionaries Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.security.NoSuchProviderException
import java.security.Provider
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


/**
 * A factory class which creates an [SSLContext] that
 * naively accepts all certificates without verification.
 *
 * <pre>
 * // Create an SSL context that naively accepts all certificates.
 * SSLContext context = NaiveSSLContext.getInstance("TLS");
 *
 * // Create a socket factory from the SSL context.
 * SSLSocketFactory factory = context.getSocketFactory();
 *
 * // Create a socket from the socket factory.
 * SSLSocket socket = factory.createSocket("www.example.com", 443);
</pre> *
 *
 * @author Takahiko Kawasaki
 */
object NaiveSSLContext {


    /**
     * Get an SSLContext that implements the specified secure
     * socket protocol and naively accepts all certificates
     * without verification.
     */
    @Throws(NoSuchAlgorithmException::class)
    fun getInstance(protocol: String): SSLContext {
        return init(SSLContext.getInstance(protocol))
    }


    /**
     * Get an SSLContext that implements the specified secure
     * socket protocol and naively accepts all certificates
     * without verification.
     */
    @Throws(NoSuchAlgorithmException::class)
    fun getInstance(protocol: String, provider: Provider): SSLContext {
        return init(SSLContext.getInstance(protocol, provider))
    }


    /**
     * Get an SSLContext that implements the specified secure
     * socket protocol and naively accepts all certificates
     * without verification.
     */
    @Throws(NoSuchAlgorithmException::class, NoSuchProviderException::class)
    fun getInstance(protocol: String, provider: String): SSLContext {
        return init(SSLContext.getInstance(protocol, provider))
    }


    /**
     * Set NaiveTrustManager to the given context.
     */
    private fun init(context: SSLContext): SSLContext {
        try {
            // Set NaiveTrustManager.
            context.init(null, arrayOf<TrustManager>(NaiveTrustManager()), null)
        } catch (e: KeyManagementException) {
            throw RuntimeException("Failed to initialize an SSLContext.", e)
        }

        return context
    }


    /**
     * A [TrustManager] which trusts all certificates naively.
     */
    private class NaiveTrustManager : X509TrustManager {
        override fun getAcceptedIssuers(): Array<X509Certificate>? {
            return null
        }


        override fun checkClientTrusted(certs: Array<X509Certificate>, authType: String) {}


        override fun checkServerTrusted(certs: Array<X509Certificate>, authType: String) {}
    }
}