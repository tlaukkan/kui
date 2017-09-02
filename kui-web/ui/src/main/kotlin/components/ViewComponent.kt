package components

import org.w3c.dom.Element
import org.w3c.dom.asList
import org.kui.server.api.users.RepeaterController

abstract class ViewComponent(templatePath: String) : TemplateComponent(templatePath) {
    val repeaters = mutableMapOf<String, RepeaterController>()
    val forms = mutableMapOf<String, FormController>()

    override fun bind() {
        super.bind()

        for (node in element!!.querySelectorAll("[repeater]").asList()) {
            val repeaterElement: Element = node as Element
            val repeaterName = repeaterElement.getAttribute("repeater")!!
            val repeaterTemplate = repeaterElement.innerHTML
            repeaters[repeaterName] = RepeaterController(this, repeaterName, repeaterTemplate, repeaterElement)
        }

        for (node in element!!.querySelectorAll("form").asList()) {
            val formElement: Element = node as Element
            val formId = formElement.getAttribute("id")!!
            forms[formId] = FormController(formId)
        }
    }

    fun getRepeater() : RepeaterController {
        return getRepeater(repeaters.keys.first())
    }

    fun getRepeater(repeaterName: String) : RepeaterController {
        return repeaters[repeaterName]!!
    }

    fun getForm(formId: String) : FormController {
        return forms[formId]!!
    }

}