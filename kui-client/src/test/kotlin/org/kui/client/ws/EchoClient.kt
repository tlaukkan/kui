package org.kui.client.ws

import java.io.*
import java.security.NoSuchAlgorithmException

import com.neovisionaries.ws.client.*

/**
 * Experimenting with WebSocket clients.
 */
object EchoClient {
    /**
     * The echo server on websocket.org.
     */
    private val SERVER = "wss://127.0.0.1:8443/ws"

    /**
     * The timeout value in milliseconds for socket connection.
     */
    private val TIMEOUT = 8443


    /**
     * The entry point of this command line application.
     */
    @Throws(Exception::class)
    @JvmStatic
    fun main(args: Array<String>) {
        // Connect to the echo server.
        val ws = connect()
        ws.sendText("Hello.")
        ws.addListener(object : WebSocketAdapter() {
            @Throws(Exception::class)
            override fun onTextMessage(websocket: WebSocket?, message: String?) {
                println(message)
            }
        })

        Thread.sleep(5000)
        ws.disconnect()
    }


    /**
     * Connect to the server.
     */
    @Throws(IOException::class, WebSocketException::class, NoSuchAlgorithmException::class)
    private fun connect(): WebSocket {
        val factory = WebSocketFactory()
        factory.sslContext = NaiveSSLContext.getInstance("TLS")

        return factory
                .setConnectionTimeout(TIMEOUT)
                .setVerifyHostname(false)
                .createSocket(SERVER)
                .addListener(object : WebSocketAdapter() {
                    // A text message arrived from the server.
                    override fun onTextMessage(websocket: WebSocket?, message: String?) {
                        println(message)
                    }
                })
                .addExtension(WebSocketExtension.PERMESSAGE_DEFLATE)
                .connect()
    }


    /**
     * Wrap the standard input with BufferedReader.
     */
    private val input: BufferedReader
        @Throws(IOException::class)
        get() = BufferedReader(InputStreamReader(System.`in`))
}