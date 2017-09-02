package views.login

import api
import components.TemplateComponent
import org.w3c.dom.HTMLInputElement
import security.context
import components.FormController

class LoginView : TemplateComponent("views/login/LoginView.html") {

    private var form: FormController? = null

    private var loginUserUsernameInput: HTMLInputElement? = null
    private var loginUserPasswordInput: HTMLInputElement? = null


    override fun bind() {
        super.bind()
        form = FormController("loginForm")
        loginUserUsernameInput = form!!.getInput("username")
        loginUserPasswordInput = form!!.getInput("password")
    }

    override fun unbind() {
        super.unbind()
    }

    fun login() {
        if (!form!!.validate()) {
            return
        }

        val key = loginUserUsernameInput!!.value
        val password = loginUserPasswordInput!!.value

        api.username = key
        api.password = password

        context.login()
    }

}