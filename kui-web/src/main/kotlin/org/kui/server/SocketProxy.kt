package org.kui.server

import javax.websocket.*
import javax.websocket.server.ServerEndpoint

@ServerEndpoint("/")
class SocketProxy {

    @OnOpen
    fun onOpen(session: Session, endointConfig: EndpointConfig) {
        session.basicRemote.sendText("Hello!!!")
        println("onOpen : $session : $endointConfig")
    }

    @OnClose
    fun onClose(session: Session, closeReason: CloseReason) {
        println("onClose : $session : $closeReason")
    }

    @OnMessage
    fun onMessage(message: String, session: Session) {
        println("onMessage: $session : $message")
    }

    @OnError
    fun onError(session: Session, throwable: Throwable) {
        println("onError: $session : $throwable")
    }

}