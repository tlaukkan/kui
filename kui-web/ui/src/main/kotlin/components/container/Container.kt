package components.container

import components.Component
import security.context
import unbindComponents
import components.navigation.navigation

var container = Container()

class Container : Component(listOf("components/container/Container.html")) {
    override fun bind() {
        container = this
        context.initialize()
    }

    override fun unbind() {
    }

    override fun refresh() {
    }

    fun setView(viewId: String) {
        if (hasElement("div")) {
            val existingChildDiv = getElement("div")
            println("Removing and unbinding: " + existingChildDiv.getAttribute("id"))
            unbindComponents(element!!)
            element!!.removeChild(existingChildDiv)
        }

        var elementHtml = getTemplate(templatePaths[0])
                .replace("{id}", viewId)
                .replace("{component}", navigation.getViewComponent(viewId))
        setInnerHtml(elementHtml)
    }

}