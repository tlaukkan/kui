package network

import org.w3c.dom.WebSocket

class WsClient(url: String) {

    var onOpen: (() -> Unit)? = null
    var onClose: (() -> Unit)? = null
    var onMessage: ((message: String) -> Unit)? = null
    var onError: (() -> Unit)? = null

    val client = WebSocket(url)

    init {
        client.onopen = {
            if (onOpen != null) {
                onOpen!!.invoke()
            }
        }

        client.onmessage = { event ->
            val dynamicEvent: dynamic = event
            if (onMessage != null) {
                onMessage!!.invoke(dynamicEvent.data)
            }
        }

        client.onerror = { event ->
            if (onError != null) {
                onError!!.invoke()
            }
        }

        client.onclose = { event ->
            if (onClose != null) {
                onClose!!.invoke()
            }
        }
    }

    fun send(text: String) {
        client.send(text)
    }

    fun close() {
        client.close()
    }

}