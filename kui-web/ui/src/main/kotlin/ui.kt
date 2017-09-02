import components.Component
import org.w3c.dom.Element
import org.w3c.dom.asList
import util.Async
import util.ErrorCallback
import util.SuccessCallback
import util.encodeURIComponent
import kotlin.browser.document
import kotlin.reflect.KClass

typealias ComponentFactory = () -> Component

private val componentRegister = mutableMapOf<Int, Component>()
private val componentFactoryRegister = mutableMapOf<String, ComponentFactory>()

private val templatePaths = mutableSetOf<String>()
private val templates = mutableMapOf<String, String>()

private var controllerIdCounter = 0

private fun reserverControllerId() : Int {
    return controllerIdCounter++
}

fun initializeUI() {
    println("Loading templates: $templatePaths ...")

    loadTemplates().error { message ->
        println("UI initialization error: " + message)
    }.success {
        bindComponents(document.body!!)
        println("UI initialization success.")
    }
}

fun loadTemplates() : Async {
    return Async({ successCallback: SuccessCallback, errorCallback: ErrorCallback ->
        val paths = templatePaths.toList()
        val pathsValueBuilder = StringBuilder()
        for (componentTemplate in paths) {
            if (pathsValueBuilder.size != 0) {
                pathsValueBuilder.append(',')
            }
            pathsValueBuilder.append(componentTemplate)
        }

        val pathsValue = encodeURIComponent(pathsValueBuilder.toString())

        api.get<Array<String>>("templates?paths=$pathsValue").error { message: String ->
            errorCallback(message)
        }.success { templateArray: Array<String> ->
            for (i in 0..templateArray.size - 1 step 2) {
                templates[templateArray[i]] = templateArray[i + 1]
            }
            println("Loaded templates: ${templates.keys}.")
            successCallback()
        }
    })
}

fun <T : Component> registerComponent(componentClass: KClass<T>, factoryMethod: () -> T) {
    val componentClassName = componentClass.simpleName!!
    if (componentFactoryRegister.containsKey(componentClassName)) {
        throw RuntimeException("Component class already registered: $componentClassName")
    }
    componentFactoryRegister[componentClassName] = factoryMethod
    val component = componentFactoryRegister[componentClassName]!!.invoke()
    templatePaths.addAll(component.templatePaths)
    println("Registered component class: $componentClassName")
}

fun bindComponents(searchRoot: Element) {
    for (node in searchRoot.querySelectorAll("[component]").asList()) {
        val element: Element = node as Element
        val controllerId = reserverControllerId()
        val controllerClassName = element.getAttribute("component")!!
        bindComponent(element, controllerClassName, controllerId)
    }
}

fun unbindComponents(searchRoot: Element) {
    for (node in searchRoot.querySelectorAll("[component]").asList()) {
        val element: Element = node as Element
        val controllerId = element.getAttribute("componentId")!!.toInt()
        val controllerClassName = element.getAttribute("component")!!
        println("Unbinding component $controllerClassName-$controllerId")
        unbindComponent(element, controllerClassName, controllerId)
    }
}

fun bindComponent(element: Element, componentClassName: String, componentId: Int) {
    if (componentFactoryRegister.containsKey(componentClassName)) {
        println("Bound component $componentClassName-$componentId")
        componentRegister[componentId] = componentFactoryRegister[componentClassName]!!()
        componentRegister[componentId]!!.bind(componentId, element)
    } else {
        println("components.Component class not registered: $componentClassName")
    }
}

fun unbindComponent(element: Element, componentClassName: String, componentId: Int) {
    componentRegister[componentId]!!.unbind()
    componentRegister.remove(componentId)
    println("Unbound component $componentClassName-$componentId")
}

@JsName("component")
fun component(id: Int) : Component {
    return componentRegister[id]!!
}

@JsName("template")
fun template(path: String) : String {
    if (!templates.containsKey(path)) {
        throw RuntimeException("Template not loaded from server: $path")
    }
    return templates[path]!!
}