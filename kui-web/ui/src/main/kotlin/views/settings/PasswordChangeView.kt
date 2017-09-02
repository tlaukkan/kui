package views.settings

import api
import components.TemplateComponent
import components.alertNotification
import components.infoNotification
import org.w3c.dom.HTMLInputElement
import org.kui.server.api.users.PasswordChange
import components.FormController

class PasswordChangeView : TemplateComponent("views/settings/PasswordChange.html") {

    private var form: FormController? = null
    private var oldPasswordInput: HTMLInputElement? = null
    private var newPasswordOneInput: HTMLInputElement? = null
    private var newPasswordTwoInput: HTMLInputElement? = null

    override fun bind() {
        super.bind()
        form = FormController("passwordChangeForm")
        oldPasswordInput = form!!.getInput("oldPasswordInput")
        newPasswordOneInput = form!!.getInput("newPasswordOneInput")
        newPasswordTwoInput = form!!.getInput("newPasswordTwoInput")
    }

    override fun unbind() {
        super.unbind()
    }


    fun change() {
        if (!form!!.validate()) {
            return
        }

        if (!newPasswordOneInput!!.value.equals(newPasswordTwoInput!!.value)) {
            alertNotification.set("New passwords do not match.")
            return
        }

        api.put("user/password", PasswordChange(oldPasswordInput!!.value, newPasswordOneInput!!.value)).error { message: String ->
            println("Error updating user password: $message")
            alertNotification.set("Old password incorrect.")
        }.success {
            println("Success updating user password.")
            oldPasswordInput!!.value = ""
            newPasswordOneInput!!.value = ""
            newPasswordTwoInput!!.value = ""
            infoNotification.set("Password changed.")
        }

    }

}