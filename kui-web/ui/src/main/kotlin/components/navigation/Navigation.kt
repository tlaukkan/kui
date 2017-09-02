package components.navigation

import components.TemplateComponent
import org.w3c.dom.Element
import org.w3c.dom.asList
import security.context
import components.container.container
import views.log.NavigationItem

var navigation = Navigation()

class Navigation : TemplateComponent("components/navigation/Navigation.html") {

    private val viewIds = mutableListOf<String>()
    private val viewComponents = mutableMapOf<String, String>()
    private val navigationItems = mutableMapOf<String, NavigationItem>()

    var viewId = ""

    override fun bind() {
        super.bind()
        navigation = this
        refresh()
    }

    override fun unbind() {
        super.unbind()
    }

    fun getViewIds() : List<String> {
        return viewIds
    }

    fun getViewComponent(viewId: String) : String {
        return viewComponents[viewId]!!
    }

    fun addView(viewId: String, viewComponent: String, navigationItem: NavigationItem) {
        viewIds.add(viewId)
        viewComponents[viewId] = viewComponent
        navigationItems[viewId] = navigationItem
        println("Added view: $viewId = $viewComponent")
    }

    fun setView(viewId: String) {
        this.viewId = viewId
        if (container != null) {
            container.setView(viewId!!)
            for (navigationItemViewId in navigationItems.keys) {
                navigationItems[navigationItemViewId]!!.setActive(navigationItemViewId.equals(viewId))
            }
        }
    }

    fun logout() {
        context.logout()
    }

    override fun refresh() {
        super.refresh()


        for (node in element!!.querySelectorAll("[roles]").asList()) {
            val childElement: Element = node as Element
            val roles = childElement.getAttribute("roles")!!.split(',').toList()

            var hidden = true
            for (role in roles) {
                if (context.getGroups().contains(role)) {
                    hidden = false
                }
            }

            if (hidden) {
                childElement.setAttribute("hidden", "")
            } else {
                childElement.removeAttribute("hidden")
            }

        }

    }
}