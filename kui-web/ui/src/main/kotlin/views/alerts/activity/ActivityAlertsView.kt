package views.alerts.activity

import api
import components.*
import org.kui.server.api.users.RepeaterController
import org.kui.server.api.users.defaultFormatter
import util.escapeUri
import util.getProperties
import util.setProperties

class ActivityAlertsView : ViewComponent("views/alerts/activity/ActivityAlertsView.html") {

    var tableRepeater: RepeaterController? = null
    var editForm: FormController? = null
    var removeForm: FormController? = null

    override fun bind() {
        super.bind()

        tableRepeater = getRepeater("tableRepeater")
        tableRepeater!!.formatters = mapOf(
                "hostType" to defaultFormatter,
                "min" to defaultFormatter,
                "max" to defaultFormatter
        )

        editForm = getForm("editForm")
        editForm!!.modalId = "editModal"
        editForm!!.getters = mapOf(
            "period" to intGetter,
            "min" to intGetter,
            "max" to intGetter
        )
        editForm!!.setters = mapOf(
            "period" to intSetter,
            "min" to intSetter,
            "max" to intSetter
        )

        removeForm = getForm("removeForm")
        removeForm!!.modalId = "removeModal"

        reload()
    }

    override fun unbind() {
    }

    private fun reload() {
        tableRepeater!!.clearRepeats()

        api.get<Array<ActivityAlert>>("safe/activityalert").error {}.success { activityAlerts: Array<ActivityAlert> ->
            for (activityAlert in activityAlerts) {
                tableRepeater!!.formatAndRepeat(getProperties(activityAlert))
            }
        }
    }

    fun add() {
        editForm!!.prepareForm(true)

        editForm!!.show()
    }

    fun edit() {
        editForm!!.prepareForm(false)

        val rowId = tableRepeater!!.getSelectedRowId() ?: return

        api.get<ActivityAlert>("safe/activityalert/${escapeUri(rowId)}").error {}.success { activityAlert: ActivityAlert ->
            val values = getProperties(activityAlert)
            editForm!!.setValues(values)
            editForm!!.show()
        }
    }

    fun save() {
        if (!editForm!!.validate()) {
            return
        }

        val values = editForm!!.getValues()
        val activityAlert = ActivityAlert()
        setProperties(activityAlert, values)

        val new = activityAlert.key!!.trim().length == 0

        if ((activityAlert.environment.isNullOrBlank() && activityAlert.hostType.isNullOrBlank() && activityAlert.host.isNullOrBlank()) ||
                (!activityAlert.environment.isNullOrBlank() && !activityAlert.hostType.isNullOrBlank() && !activityAlert.host.isNullOrBlank()) ||
                (!activityAlert.environment.isNullOrBlank() && activityAlert.hostType.isNullOrBlank() && !activityAlert.host.isNullOrBlank()) ||
                (activityAlert.environment.isNullOrBlank() && !activityAlert.hostType.isNullOrBlank())
                ) {
            alertNotification.set("Please enter environment type alone, environment type & host type or host alone.")
            return
        }

        if (new) {
            activityAlert.key = "${escapeUri(activityAlert.environment!!)}.${escapeUri(activityAlert.hostType!!)}..${escapeUri(activityAlert.host!!)}.${escapeUri(activityAlert.log!!)}.${escapeUri(activityAlert.tag!!)}".toLowerCase()
            api.post("safe/activityalert", activityAlert).error {}.success {
                reload()
                editForm!!.hide()
                editForm!!.clearInputs()
            }
        } else {
            api.put("safe/activityalert/${escapeUri(activityAlert.key!!)}", activityAlert).error {}.success {
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

        api.get<ActivityAlert>("safe/activityalert/${escapeUri(rowId)}").error {}.success { activityAlert: ActivityAlert ->
            val values = getProperties(activityAlert)
            removeForm!!.setValues(values)
            removeForm!!.show()
        }
    }

    fun remove() {
        val values = removeForm!!.getValues()
        api.delete("safe/activityalert/${escapeUri(values["key"]!! as String)}").error {}.success {
            reload()
            removeForm!!.hide()
        }
    }

}