package views.taggers

import api
import components.ViewComponent
import org.w3c.dom.Element
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.asList
import util.dateToShortUiString
import util.escapeUri
import util.getDateFromMilliseconds
import components.FormController
import kotlin.browser.document

class TaggersView : ViewComponent("views/taggers/TaggersView.html") {

    var addForm: FormController? = null

    var addEnvironmentInput: HTMLInputElement? = null
    var addHostInput: HTMLInputElement? = null
    var addLogInput: HTMLInputElement? = null
    var addPatternInput: HTMLInputElement? = null
    var addTagInput: HTMLInputElement? = null
    var addColorInput: HTMLInputElement? = null

    var editForm: FormController? = null

    var editKeyInput: HTMLInputElement? = null
    var editEnvironmentInput: HTMLInputElement? = null
    var editHostInput: HTMLInputElement? = null
    var editLogInput: HTMLInputElement? = null
    var editPatternInput: HTMLInputElement? = null
    var editTagInput: HTMLInputElement? = null
    var editColorInput: HTMLInputElement? = null

    override fun bind() {
        super.bind()

        addForm = FormController("addForm")

        addEnvironmentInput = addForm!!.getInput("addEnvironmentInput")
        addHostInput = addForm!!.getInput("addHostInput")
        addLogInput = addForm!!.getInput("addLogInput")
        addPatternInput = addForm!!.getInput("addPatternInput")
        addTagInput = addForm!!.getInput("addTagInput")
        addColorInput = addForm!!.getInput("addColorInput")

        editForm = FormController("editForm")

        editKeyInput = editForm!!.getInput("editKeyInput")
        editEnvironmentInput = editForm!!.getInput("editEnvironmentInput")
        editHostInput = editForm!!.getInput("editHostInput")
        editLogInput = editForm!!.getInput("editLogInput")
        editPatternInput = editForm!!.getInput("editPatternInput")
        editTagInput = editForm!!.getInput("editTagInput")
        editColorInput = editForm!!.getInput("editColorInput")

        reload()
    }

    override fun unbind() {
    }

    private fun reload() {
        getRepeater().clearRepeats()

        api.get<Array<Tagger>>("safe/tagger").error { message: String ->
            println("Error: $message")
        }.success { taggers: Array<Tagger> ->
            for (tagger in taggers) {
                getRepeater().repeat(mapOf(
                        "modelId" to tagger.key!!,
                        "environment" to tagger.environment!!,
                        "host" to tagger.host!!,
                        "log" to tagger.log!!,
                        "pattern" to tagger.pattern!!,
                        "tag" to tagger.tag!!,
                        "color" to tagger.color!!,
                        "modified" to dateToShortUiString(getDateFromMilliseconds(tagger.modified!!))
                ))
            }
        }
    }

    fun add() {
        js("$('#addModal').modal()")
    }

    fun create() {
        if (!addForm!!.validate()) {
            return
        }

        val tagger = Tagger(
                key = "${escapeUri(addEnvironmentInput!!.value)}.${escapeUri(addHostInput!!.value)}.${escapeUri(addLogInput!!.value)}.${escapeUri(addTagInput!!.value)}".toLowerCase(),
                environment = addEnvironmentInput!!.value,
                host = addHostInput!!.value,
                log = addLogInput!!.value,
                pattern = addPatternInput!!.value,
                tag = addTagInput!!.value,
                color = addColorInput!!.value)

        api.post("safe/tagger", tagger).error { message: String ->
            println("Error adding user: $message")
        }.success {
            reload()

            js("$('#addModal').modal('hide')")

            addForm!!.clearInputs()
        }

    }

    fun edit() {
        for (node in element!!.querySelectorAll("input:checked").asList()) {
            val checkedElement: Element = node as Element
            val modelId = escapeUri(checkedElement.getAttribute("modelId")!!)

            api.get<Tagger>("safe/tagger/${modelId}").error { message: String ->
                println("Error getting tagger: $message")
            }.success { tagger: Tagger ->
                editKeyInput!!.value = tagger.key!!
                editEnvironmentInput!!.value = tagger.environment!!
                editHostInput!!.value = tagger.host!!
                editLogInput!!.value = tagger.log!!
                editPatternInput!!.value = tagger.pattern!!
                editTagInput!!.value = tagger.tag!!
                editColorInput!!.value = tagger.color!!

                js("$('#editModal').modal()")
            }

            break
        }
    }

    fun update() {
        if (!editForm!!.validate()) {
            return
        }

        val tagger = Tagger(
                key = editKeyInput!!.value,
                environment = editEnvironmentInput!!.value,
                host = editHostInput!!.value,
                log = editLogInput!!.value,
                pattern = editPatternInput!!.value,
                tag = editTagInput!!.value,
                color = editColorInput!!.value)

        api.put("safe/tagger/${escapeUri(tagger.key!!)}", tagger).error { message: String ->
            println("Error updating tagger: $message")
        }.success {
            reload()

            js("$('#editModal').modal('hide')")

            editForm!!.clearInputs()
        }
    }

    fun remove() {
        for (node in element!!.querySelectorAll("input:checked").asList()) {
            val checkedElement: Element = node as Element
            val modelId = checkedElement.getAttribute("modelId")

            api.get<Tagger>("safe/tagger/${escapeUri(modelId!!)}").error { message: String ->
                println("Error getting tagger: $message")
            }.success { tagger: Tagger ->
                val removeKeyInput = document.getElementById("removeKeyInput") as HTMLInputElement
                removeKeyInput.value = tagger.key!!
                js("$('#removeModal').modal()")
            }

            break
        }
    }

    fun delete() {
        val removeKeyInput = document.getElementById("removeKeyInput") as HTMLInputElement
        val key = removeKeyInput.value

        api.delete("safe/tagger/${escapeUri(key)}").error { message: String ->
            println("Error removing tagger: $message")
        }.success {
            reload()
            println("Success removing tagger.")
            js("$('#removeModal').modal('hide')")
        }
    }

}