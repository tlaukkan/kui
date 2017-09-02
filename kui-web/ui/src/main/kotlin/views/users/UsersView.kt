package views.users

import api
import components.ViewComponent
import org.w3c.dom.Element
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.asList
import org.kui.server.api.users.User
import util.dateToUiString
import util.getDateFromMilliseconds
import components.FormController
import kotlin.browser.document

class UsersView : ViewComponent("views/users/UsersView.html") {

    var addUserForm: FormController? = null

    var addUserUsernameInput: HTMLInputElement? = null
    var addUserEmailInput: HTMLInputElement? = null
    var addUserPasswordInput: HTMLInputElement? = null
    var addUserAdminInput: HTMLInputElement? = null

    var editUserForm: FormController? = null

    var editUserUsernameInput: HTMLInputElement? = null
    var editUserEmailInput: HTMLInputElement? = null
    var editUserPasswordInput: HTMLInputElement? = null
    var editUserAdminInput: HTMLInputElement? = null

    override fun bind() {
        super.bind()

        addUserForm = FormController("addUserForm")
        addUserUsernameInput = addUserForm!!.getInput("addUserUsernameInput")
        addUserEmailInput = addUserForm!!.getInput("addUserEmailInput")
        addUserPasswordInput = addUserForm!!.getInput("addUserPasswordInput")
        addUserAdminInput = addUserForm!!.getInput("addUserAdminInput")

        editUserForm = FormController("editUserForm")

        editUserUsernameInput = editUserForm!!.getInput("editUserUsernameInput")
        editUserEmailInput = editUserForm!!.getInput("editUserEmailInput")
        editUserPasswordInput = editUserForm!!.getInput("editUserPasswordInput")
        editUserAdminInput = editUserForm!!.getInput("editUserAdminInput")

        reload()
    }

    override fun unbind() {
    }

    private fun reload() {
        getRepeater().clearRepeats()

        api.get<Array<User>>("users").error { message: String ->
            println("Error: $message")
        }.success { users: Array<User> ->
            for (user in users) {
                val groups = StringBuilder("")
                for (group in user.groups!!) {
                    if (groups.length > 0) {
                        groups.append(", ")
                    }
                    groups.append(group)
                }
                getRepeater().repeat(mapOf(
                        "username" to user.username!!,
                        "email" to (user.email ?: "-"),
                        "groups" to groups.toString(),
                        "created" to dateToUiString(getDateFromMilliseconds(user.created!!)),
                        "modified" to dateToUiString(getDateFromMilliseconds(user.modified!!))
                ))
            }
        }
    }

    fun add() {
        js("$('#addUserModal').modal()")
    }

    fun create() {

        if (!addUserForm!!.validate()) {
            return
        }

        val username = addUserUsernameInput!!.value
        val email = addUserEmailInput!!.value
        var password : String? = addUserPasswordInput!!.value
        val admin : Boolean = addUserAdminInput!!.checked

        // Ensure no empty password is saved.
        if (password!!.length == 0) {
            password = null
        }

        val groups = mutableListOf<String>()
        if (admin) {
            groups.add("admin")
        }

        val user = User(username = username, email = email, password = password, groups = groups.toTypedArray())

        api.post("users", user).error { message: String ->
            println("Error adding user: $message")
        }.success {
            reload()

            js("$('#addUserModal').modal('hide')")

            addUserForm!!.clearInputs()
        }

    }

    fun edit() {
        for (node in element!!.querySelectorAll("input:checked").asList()) {
            val checkedElement: Element = node as Element
            val modelId = checkedElement.getAttribute("modelId")

            api.get<User>("users/${modelId}").error { message: String ->
                println("Error getting user: $message")
            }.success { user: User ->
                editUserUsernameInput!!.value = user.username!!
                editUserEmailInput!!.value = user.email ?: ""
                editUserPasswordInput!!.value = ""
                editUserAdminInput!!.checked = user.groups!!.contains("admin")

                js("$('#editUserModal').modal()")
            }

            break
        }
    }

    fun update() {
        if (!editUserForm!!.validate()) {
            return
        }

        val username = editUserUsernameInput!!.value
        val email = editUserEmailInput!!.value
        val password = editUserPasswordInput!!.value
        val admin : Boolean = editUserAdminInput!!.checked

        val groups = mutableListOf<String>()
        if (admin) {
            groups.add("admin")
        }

        val user = User(username = username, email = email, groups = groups.toTypedArray())

        if (password.length != 0) {
            user.password = password
        }

        api.put("users/$username", user).error { message: String ->
            println("Error updating user: $message")
        }.success {
            reload()
            println("Success updating user.")

            editUserForm!!.clearInputs()

            js("$('#editUserModal').modal('hide')")
        }

    }

    fun remove() {
        for (node in element!!.querySelectorAll("input:checked").asList()) {
            val checkedElement: Element = node as Element
            val modelId = checkedElement.getAttribute("modelId")

            api.get<User>("users/${modelId}").error { message: String ->
                println("Error getting user: $message")
            }.success { user: User ->
                val removeUserUsernameInput = document.getElementById("removeUserUsernameInput") as HTMLInputElement
                removeUserUsernameInput.value = user.username!!
                js("$('#removeUserModal').modal()")
            }

            break
        }
    }

    fun delete() {
        val removeUserUsernameInput = document.getElementById("removeUserUsernameInput") as HTMLInputElement
        val username = removeUserUsernameInput.value

        api.delete("users/${username}").error { message: String ->
            println("Error removing user: $message")
        }.success {
            reload()
            println("Success removing user.")
            js("$('#removeUserModal').modal('hide')")
        }
    }

}