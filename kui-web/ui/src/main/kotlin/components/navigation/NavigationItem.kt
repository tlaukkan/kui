package views.log

import components.Component
import components.navigation.Navigation

class NavigationItem : Component() {

    var viewId : String? = null

    override fun bind() {
        val viewComponent = this.element!!.getAttribute("view")!!
        val navigation: Navigation = getTargetComponentByElementId("navigation")!!

        val link = getElement("a")!!
        link.setAttribute("onclick", "ui.component(${this.id}).navigate()")
        val href = link.getAttribute("href")!!
        viewId = href.substring(href.indexOf("#")+1)

        navigation.addView(viewId!!, viewComponent, this)
    }

    override fun unbind() {
    }

    override fun refresh() {
    }

    fun navigate() {
        val navigation: Navigation = getTargetComponentByElementId("navigation")!!
        navigation.setView(viewId!!)
    }

    fun setActive(active: Boolean) {
        val link = getElement("a")!!
        if (active) {
            if (!link.classList.contains("active")) {
                link.classList.add("active")
            }
        } else {
            if (link.classList.contains("active")) {
                link.classList.remove("active")
            }
        }
    }

}