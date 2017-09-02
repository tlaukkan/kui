package views.alerts.activity

import api
import components.*
import org.kui.security.model.Host
import org.kui.server.api.users.RepeaterController
import util.escapeUri
import util.getProperties
import util.setProperties

class HostsView : ViewComponent("views/hosts/HostsView.html") {

    var apiUrlPostFix = "safe/host"

    var mode: Boolean = false

    var tableRepeater: RepeaterController? = null
    var editForm: FormController? = null
    var removeForm: FormController? = null

    override fun bind() {
        super.bind()

        tableRepeater = getRepeater("tableRepeater")

        editForm = getForm("editForm")
        editForm!!.modalId = "editModal"

        removeForm = getForm("removeForm")
        removeForm!!.modalId = "removeModal"

        reload()
    }

    override fun unbind() {
    }

    private fun reload() {
        tableRepeater!!.clearRepeats()

        api.get<Array<Host>>(apiUrlPostFix).error {}.success { records: Array<Host> ->
            for (record in records) {
                tableRepeater!!.formatAndRepeat(getProperties(record))
            }
        }
    }

    fun add() {
        mode = true
        editForm!!.prepareForm(mode)

        editForm!!.show()
    }

    fun edit() {
        mode = false
        editForm!!.prepareForm(mode)

        val rowId = tableRepeater!!.getSelectedRowId() ?: return

        api.get<Host>("$apiUrlPostFix/${escapeUri(rowId)}").error {}.success { record: Host ->
            val values = getProperties(record)
            editForm!!.setValues(values)
            editForm!!.show()
        }
    }

    fun save() {
        if (!editForm!!.validate()) {
            return
        }

        val values = editForm!!.getValues()
        val record = Host()
        setProperties(record, values)

        if (mode) {
            api.post(apiUrlPostFix, record).error {}.success {
                reload()
                editForm!!.hide()
                editForm!!.clearInputs()
            }
        } else {
            api.put("$apiUrlPostFix/${escapeUri(record.key!!)}", record).error {}.success {
                reload()
                editForm!!.hide()
                editForm!!.clearInputs()
            }
        }
    }

    fun cancel() {
        editForm!!.hide()
        editForm!!.clearInputs()
    }

    fun removeConfirmation() {
        val rowId = tableRepeater!!.getSelectedRowId() ?: return

        api.get<Host>("$apiUrlPostFix/${escapeUri(rowId)}").error {}.success { record: Host ->
            val values = getProperties(record)
            removeForm!!.setValues(values)
            removeForm!!.show()
        }
    }

    fun remove() {
        val values = removeForm!!.getValues()
        api.delete("$apiUrlPostFix/${escapeUri(values["key"]!! as String)}").error {}.success {
            reload()
            removeForm!!.hide()
        }
    }

}