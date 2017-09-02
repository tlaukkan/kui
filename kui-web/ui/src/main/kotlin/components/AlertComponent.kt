package components

import org.w3c.dom.asList
import kotlin.browser.window

/**
 * Created by tlaukkan on 7/19/2017.
 */
open class AlertComponent : Component() {
    var messageCounter = 0
    override fun bind() {
        injectReferences()
    }

    override fun unbind() {
    }

    override fun refresh() {
    }

    fun set(message: String) {
        this.messageCounter++
        val currentMessageCounter = messageCounter
        element!!.getElementsByTagName("div").asList()[0].innerHTML = message
        if (message.size != 0) {
            element!!.removeAttribute("hidden")

            window.setTimeout({
                if (currentMessageCounter == messageCounter) {
                    clear()
                }
            }, 3000)

        } else {
            element!!.setAttribute("hidden", "")
        }
    }

    fun clear() {
        set("")
    }
}