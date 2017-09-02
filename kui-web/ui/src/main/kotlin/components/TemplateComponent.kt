package components

import org.w3c.dom.Element
import org.w3c.dom.asList

open class TemplateComponent(templatePath: String) : Component(listOf(templatePath)) {
    override fun bind() {
        setInnerHtml(getTemplate(templatePaths[0]))

        injectReferences()
    }

    override fun unbind() {
    }

    override fun refresh() {
    }
}