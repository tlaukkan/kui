package org.kui.server.api.users

import components.ViewComponent
import org.w3c.dom.Element
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.asList
import util.escapeHtml

val defaultFormatter : (Any?) -> String = { value -> value?.toString() ?: "" }

class RepeaterController(
        var parent: ViewComponent,
        var name: String,
        var template: String,
        var element: Element,
        var formatters: Map<String, (Any?) -> String> = emptyMap()
) {
    var keys = getTemplateKeys()

    init {
        clearRepeats()
    }

    fun getSelectedRowId() : String? {
        val selectedElement: Element = getCheckedInput() ?: return null
        val selectedRowId = selectedElement.getAttribute("rowId")
        return selectedRowId
    }

    fun getSelectedRowIds() : List<String> {
        val selectedRowIds = mutableListOf<String>()
        for (selectedElement in getCheckedInputs()) {
            selectedRowIds.add(selectedElement.getAttribute("rowId")!!)
        }
        return selectedRowIds
    }


    private fun getCheckedInput() : HTMLInputElement? {
        for (node in element!!.querySelectorAll("input:checked").asList()) {
            return node as HTMLInputElement
        }
        return null
    }

    private fun getCheckedInputs() : List<HTMLInputElement> {
        val checkedInput = mutableListOf<HTMLInputElement>()
        for (node in element!!.querySelectorAll("input:checked").asList()) {
            checkedInput.add(node as HTMLInputElement)
        }
        return checkedInput
    }

    private fun getTemplateKeys() : Set<String> {
        val elementHtml = template!!
        val regex = Regex("{[a-zA-Z]*}")
        val matches = regex.findAll(elementHtml)
        val keys = mutableSetOf<String>()
        for (match in matches) {
            keys.add(match.value.substring(1, match.value.length - 1))
        }
        return keys
    }

    fun formatValues(values: Map<String, Any?>): MutableMap<String, String> {
        val convertedValues = mutableMapOf<String, String>()
        for (key in keys) {
            if (formatters.containsKey(key)) {
                convertedValues[key] = formatters[key]!!(values[key])
            } else {
                convertedValues[key] = defaultFormatter(values[key])
            }
        }
        return convertedValues
    }


    fun clearRepeats() {
        while (element.lastChild != null) {
            element.removeChild(element.lastChild!!)
        }
    }

    fun formatAndRepeat(values: Map<String, Any?>) {
        repeat(formatValues(values))
    }

    fun repeat(values: Map<String, String>) {
        var elementHtml = template
        for (value in values) {
            elementHtml = elementHtml.replace("{${value.key}}", escapeHtml(value.value))
        }
        elementHtml = elementHtml.replace("{id}", escapeHtml("${parent.id}:$name:${element.childElementCount}"))
        parent.appendElement(element, elementHtml)
    }

    fun repeatWithTagName(tagName: String, values: Map<String, String>) {
        var elementHtml = template
        for (value in values) {
            elementHtml = elementHtml.replace("{${value.key}}", escapeHtml(value.value))
        }
        elementHtml = elementHtml.replace("{id}", escapeHtml("${parent.id}:$name:${element.childElementCount}"))
        parent.appendElement(element, tagName, elementHtml)
    }

    fun repeatEscaped(tagName: String, values: Map<String, String>) {
        var elementHtml = template
        for (value in values) {
            elementHtml = elementHtml.replace("{${value.key}}", value.value)
        }
        elementHtml = elementHtml.replace("{id}", escapeHtml("${parent.id}:$name:${element.childElementCount}"))
        parent.appendElement(element, tagName, elementHtml)
    }

    fun repeaterRowHtml(values: Map<String, String>) : String {
        var elementHtml = template
        for (value in values) {
            elementHtml = elementHtml.replace("{${value.key}}", value.value)
        }
        return elementHtml
    }

    fun repeatAppendFragment(html: String) {
        parent.appendAsFragment(element, html)
    }

    fun repeatHtml(html: String) {
        parent.appendElement(element, html)
    }

}