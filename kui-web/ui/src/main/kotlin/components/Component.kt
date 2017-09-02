package components

import bindComponents
import component
import org.w3c.dom.*
import security.Context
import security.context
import template
import kotlin.browser.document
import kotlin.dom.addClass
import kotlin.dom.removeClass

abstract class Component(val templatePaths: List<String> = listOf<String>()) {

    var id: Int? = null
    var element: Element? = null

    fun bind(id: Int, element: Element) : Component{
        this.id = id
        this.element = element
        element.setAttribute("componentId", id.toString())
        bind()
        return this
    }

    abstract fun bind() : Unit

    abstract fun unbind() : Unit

    abstract fun refresh() : Unit

    fun getTemplate(templatePath: String) : String {
        if (!templatePaths.contains(templatePath)) {
            throw RuntimeException("Template path not submitted in constructor for pre-loading $templatePath.")
        }
        return template(templatePath)
    }

    protected fun hasElement(tagName: String) : Boolean {
        val child = element!!.getElementsByTagName(tagName)[0]
        return child != null
    }

    protected fun getElement(tagName: String) : Element {
        val child = element!!.getElementsByTagName(tagName)[0]
        if (child == null) {
            throw RuntimeException("No child element found with tag name: $tagName")
        }
        return child!!
    }

    fun appendElement(parentTagName: String, elementHtml: String) {
        val parent: Element = getElement(parentTagName)
        appendElement(parent, elementHtml)
    }

    fun appendElement(parent: Element, elementHtml: String) {
        val element = document.createElement("div")
        parent.appendChild(element)
        element.outerHTML = elementHtml
    }

    fun appendElement(parent: Element, elementTagName: String, elementHtml: String) {
        val element = document.createElement(elementTagName)
        parent.appendChild(element)
        element.outerHTML = elementHtml
    }

    fun appendAsFragment(parent: Element, elementInnerHtml: String) {
        val element = document.createElement("div") as HTMLDivElement
        element.innerHTML = elementInnerHtml
        val documentFragment = document.createDocumentFragment()
        documentFragment.appendChild(element)
        /*for (child in element.children.asList()) {
            documentFragment.appendChild(child)
        }*/
        parent.appendChild(documentFragment)
        //parent.innerHTML+=elementInnerHtml
    }

    protected fun setInnerHtml(html: String) {
        element!!.innerHTML = html
        bindComponents(element!!)
    }

    protected fun <T : Component> getTargetComponentByElementId(targetId: String): T? {
        val targetElement = document.getElementById(targetId)!!
        val targetComponentId = targetElement.getAttribute("componentId")!!
        return component(targetComponentId.toInt()) as T?
    }

    fun getContext() : Context {
        return context
    }

    protected fun injectReferences() {
        if (element!!.hasAttribute("onclick")) {
            injectReferences(element!!, "onclick")
        }
        if (element!!.hasAttribute("onfocusout")) {
            injectReferences(element!!, "onfocusout")
        }
        for (node in element!!.querySelectorAll("[onclick]").asList()) {
            val childElement: Element = node as Element
            injectReferences(childElement, "onclick")
        }
        for (node in element!!.querySelectorAll("[onfocusout]").asList()) {
            val childElement: Element = node as Element
            injectReferences(childElement, "onfocusout")
        }
    }

    private fun injectReferences(targetElement: Element, attributeName: String) {
        val functionString = targetElement.getAttribute(attributeName)!!
        if (functionString.contains("component.")) {
            targetElement.setAttribute(attributeName, functionString.replace("component.", "ui.component($id)."))
        }
    }

}
