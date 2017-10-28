package org.kui.ws;

import java.io.*;
import java.security.NoSuchAlgorithmException;

import com.neovisionaries.ws.client.*;


public class EchoClient
{
    /**
     * The echo server on websocket.org.
     */
    private static final String SERVER = "wss://127.0.0.1:8443/ws";

    /**
     * The timeout value in milliseconds for socket connection.
     */
    private static final int TIMEOUT = 8443;


    /**
     * The entry point of this command line application.
     */
    public static void main(String[] args) throws Exception
    {
        // Connect to the echo server.
        WebSocket ws = connect();

        // The standard input via BufferedReader.
        BufferedReader in = getInput();

        ws.sendText("Hello.");

        ws.addListener(new WebSocketAdapter() {
            @Override
            public void onTextMessage(WebSocket websocket, String message) throws Exception {
                System.out.println(message);
            }
        });

        Thread.sleep(5000);

        // Close the WebSocket.
        ws.disconnect();
    }


    /**
     * Connect to the server.
     */
    private static WebSocket connect() throws IOException, WebSocketException, NoSuchAlgorithmException
    {
        WebSocketFactory factory = new WebSocketFactory();
        factory.setSSLContext(NaiveSSLContext.getInstance("TLS"));

        return factory
            .setConnectionTimeout(TIMEOUT)
            .setVerifyHostname(false)
            .createSocket(SERVER)
            .addListener(new WebSocketAdapter() {
                // A text message arrived from the server.
                public void onTextMessage(WebSocket websocket, String message) {
                    System.out.println(message);
                }
            })
            .addExtension(WebSocketExtension.PERMESSAGE_DEFLATE)
            .connect();
    }


    /**
     * Wrap the standard input with BufferedReader.
     */
    private static BufferedReader getInput() throws IOException
    {
        return new BufferedReader(new InputStreamReader(System.in));
    }
}