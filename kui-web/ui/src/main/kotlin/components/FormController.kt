package components

import org.w3c.dom.Element
import org.w3c.dom.HTMLFormElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.asList
import util.hideModal
import util.showModal
import kotlin.browser.document
import kotlin.dom.addClass
import kotlin.dom.removeClass

val intSetter : (HTMLInputElement, Any?) -> Unit = { input, value -> input.value = value?.toString() ?: ""; ; }
val intGetter = { input : HTMLInputElement -> if (input!!.value != null && input!!.value.length > 0) input!!.value.toInt() else null }

class FormController(id: String, var modalId: String = "",
                     var getters: Map<String, (HTMLInputElement) -> Any?> = emptyMap(),
                     var setters: Map<String, (HTMLInputElement, Any?) -> Unit> = emptyMap()) {

    var form: HTMLFormElement = document.getElementById(id) as HTMLFormElement
    val idInputMap = mutableMapOf<String, HTMLInputElement>()
    val modelKeyInputMap = mutableMapOf<String, HTMLInputElement>()

    init {
        for (node in form.querySelectorAll("input").asList()) {
            val element = node as HTMLInputElement
            idInputMap.put(element.id, element)
            if (element.hasAttribute("model")) {
                modelKeyInputMap.put(element.getAttribute("model")!!, element)
            }
        }
    }

    fun getInput(elementId: String) : HTMLInputElement {
        return idInputMap.get(elementId)!!
    }

    fun getModelKeys() : Set<String> {
        return modelKeyInputMap.keys
    }

    fun getInputByModelKeys(model: String) : HTMLInputElement {
        return modelKeyInputMap.get(model)!!
    }

    fun prepareForm(addMode: Boolean) {

        for (input in idInputMap.values) {
            if (input.hasAttribute("editreadonly")) {
                if (!addMode && !input.hasAttribute("readonly")) {
                    input.setAttribute("readonly", "")
                }
                if (addMode && input.hasAttribute("readonly")) {
                    input.removeAttribute("readonly")
                }
            }
        }

    }

    fun clearInputs() {
        for (input in idInputMap.values) {
            clearValidateInput(input)
            input.value = ""
        }
        form!!.reset()
    }

    fun validate(): Boolean {
        for (input in idInputMap.values) {
            validateInput(input)
        }
        return form.checkValidity()
    }

    private fun validateInput(inputElement: HTMLInputElement) : Boolean {
        if (!inputElement.checkValidity()) {
            inputElement.parentElement!!.addClass("has-warning")
            inputElement.addClass("form-control-warning")
            val feedbackElements = inputElement.parentElement!!.querySelectorAll(".form-control-feedback").asList()
            if (feedbackElements.size > 0) {
                (feedbackElements[0] as Element).removeAttribute("hidden")
            }
            return false
        } else {
            inputElement.parentElement!!.removeClass("has-warning")
            inputElement.removeClass("form-control-warning")
            val feedbackElements = inputElement.parentElement!!.querySelectorAll(".form-control-feedback").asList()
            if (feedbackElements.size > 0) {
                (feedbackElements[0] as Element).setAttribute("hidden", "")
            }
            return true
        }
    }

    private fun clearValidateInput(inputElement: HTMLInputElement) {
        inputElement.parentElement!!.removeClass("has-warning")
        inputElement.removeClass("form-control-warning")
        val feedbackElements = inputElement.parentElement!!.querySelectorAll(".form-control-feedback").asList()
        if (feedbackElements.size > 0) {
            (feedbackElements[0] as Element).setAttribute("hidden", "")
        }
    }

    fun show() {
        showModal(modalId)
    }

    fun hide() {
        hideModal(modalId)
    }

    fun getValues(): MutableMap<String, Any?> {
        val values = mutableMapOf<String, Any?>()
        for (key in modelKeyInputMap.keys) {
            val input = getInputByModelKeys(key)

            if (getters.containsKey(key)) {
                values[key] = getters[key]!!(input)
            } else {
                values[key] = input.value
            }

        }
        return values
    }

    fun setValues(values: Map<String, Any?>) {
        for (key in modelKeyInputMap.keys) {
            val input = getInputByModelKeys(key)
            val value = values[key]

            if (setters.containsKey(key)) {
                setters[key]!!(input, value)
            } else {
                input.value = value as String
            }

        }
    }


}