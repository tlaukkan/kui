package security

import api
import components.alertNotification
import org.kui.server.api.users.User
import components.container.container
import components.navigation.navigation
import kotlin.browser.sessionStorage
import kotlin.browser.window

val context = Context()

class Context {
    var user: User? = null

    fun getUsername() : String {
        return user?.username!!
    }

    fun getGroups() : List<String> {
        if (user == null) {
            return listOf("anonymous")
        } else {
            return user!!.groups!!.toList()
        }
    }

    fun login() {
        api.get<User>("security/context").error { message: String ->
            alertNotification.set("Incorrect user name or password.")
        }.success { user: User ->
            println("Signed in: ${user.username}")
            setUser(user)
        }
    }

    fun initialize() {
        val securityToken = sessionStorage.getItem("Security-Token")
        if (securityToken != null && securityToken.length != 0) {
            api.securityToken = securityToken
            api.get<User>("security/context").error { message: String ->
                alertNotification.set("Security token expired.")
                container.setView("login")
            }.success { user: User ->
                setUser(user)
            }
        } else {
            container.setView("login")
        }
    }

    private fun setUser(user: User) {
        println("Set user to: ${user.username}")
        context.user = user
        navigation.refresh()
        var hash = window.location.hash.substring(1)
        if (hash.indexOf("?") > -1) {
            hash = hash.substring(0, hash.indexOf("?"))
        }
        if (hash.length != 0) {
            navigation.setView(hash)
        } else {
            navigation.setView(navigation.getViewIds()[0])
        }
    }

    fun logout() {
        api.delete("security/context", api.securityToken!!).error { message: String ->
            println("Error signing out: $message")
        }.success {
            context.user = null
            sessionStorage.removeItem("Security-Token")
            api.securityToken = null
            println("Signed out.")
            container.setView("login")
            navigation.refresh()
        }
    }

    fun clear() {
        context.user = null
        sessionStorage.removeItem("Security-Token")
        if (api.securityToken != null) {
            alertNotification.set("Security token expired.")
        }
        api.securityToken = null
        container.setView("login")
        navigation.refresh()
    }

}