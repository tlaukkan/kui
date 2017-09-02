package network

import components.errorNotification
import org.w3c.dom.events.Event
import org.w3c.dom.set
import org.w3c.xhr.XMLHttpRequest
import security.context
import util.*
import kotlin.browser.sessionStorage

class RestClient(var apiUrl: String) {

    var username: String? = null
    var password: String? = null
    var securityToken: String? = null

    fun <T : Any> get(resourceUrlFragment: String) : ResultAsync<T> {
        return ResultAsync({ successCallback: SuccessResultCallback<T>, errorCallback: ErrorCallback ->
            val url = "$apiUrl/$resourceUrlFragment"
            val xmlHttp = XMLHttpRequest()
            xmlHttp.open("GET", "$apiUrl/$resourceUrlFragment", true)
            setAuthorizationHeader(xmlHttp)
            xmlHttp.onreadystatechange = fun (event : Event) : Unit {
                if (xmlHttp.readyState as Int == 4) {
                    if (xmlHttp.status as Int == 200) {
                        collectSecurityToken(xmlHttp)
                        successCallback(fromJson<T>(xmlHttp.responseText))
                    } else {
                        if (((xmlHttp.status as Int) >= 500) && ((xmlHttp.status as Int) < 600)) {
                            errorNotification.set("Server error.")
                        }
                        if (xmlHttp.status as Int == 401) {
                            context.clear()
                        }
                        val errorMessage = "Response code ${xmlHttp.status} for REST API GET at $url."
                        errorCallback(errorMessage)
                    }
                }
                return
            }
            xmlHttp.send()
        })
    }

    fun <T : Any> post(resourceUrlFragment: String, value: T) : Async {
        return Async({ successCallback: SuccessCallback, errorCallback: ErrorCallback ->
            val url = "$apiUrl/$resourceUrlFragment"
            val xmlHttp = XMLHttpRequest()
            xmlHttp.open("POST", "$apiUrl/$resourceUrlFragment", true)
            setAuthorizationHeader(xmlHttp)
            xmlHttp.onreadystatechange = fun (event : Event) : Unit {
                if (xmlHttp.readyState as Int == 4) {
                    if (xmlHttp.status as Int == 200) {
                        collectSecurityToken(xmlHttp)
                        successCallback()
                    } else {
                        if (((xmlHttp.status as Int) >= 500) && ((xmlHttp.status as Int) < 600)) {
                            errorNotification.set("Server error.")
                        }
                        if (xmlHttp.status as Int == 401) {
                            context.clear()
                            return
                        }
                        val errorMessage = "Response code ${xmlHttp.status} for REST API POST at $url."
                        errorCallback(errorMessage)
                    }
                }
                return
            }
            xmlHttp.send(toJson(value))
        })
    }

    fun <T : Any> put(resourceUrlFragment: String, value: T) : Async {
        return Async({ successCallback: SuccessCallback, errorCallback: ErrorCallback ->
            val url = "$apiUrl/$resourceUrlFragment"
            val xmlHttp = XMLHttpRequest()
            xmlHttp.open("PUT", "$apiUrl/$resourceUrlFragment", true)
            setAuthorizationHeader(xmlHttp)
            xmlHttp.onreadystatechange = fun (event : Event) : Unit {
                if (xmlHttp.readyState as Int == 4) {
                    if (xmlHttp.status as Int == 200) {
                        collectSecurityToken(xmlHttp)
                        successCallback()
                    } else {
                        if (((xmlHttp.status as Int) >= 500) && ((xmlHttp.status as Int) < 600)) {
                            errorNotification.set("Server error.")
                        }
                        if (xmlHttp.status as Int == 401) {
                            context.clear()
                            return
                        }
                        val errorMessage = "Response code ${xmlHttp.status} for REST API PUT at $url."
                        errorCallback(errorMessage)
                    }
                }
                return
            }
            xmlHttp.send(toJson(value))
        })
    }

    fun delete(resourceUrlFragment: String, value: Any? = null) : Async {
        return Async({ successCallback: SuccessCallback, errorCallback: ErrorCallback ->
            val url = "$apiUrl/$resourceUrlFragment"
            val xmlHttp = XMLHttpRequest()
            xmlHttp.open("DELETE", "$apiUrl/$resourceUrlFragment", true)
            setAuthorizationHeader(xmlHttp)
            xmlHttp.onreadystatechange = fun (event : Event) : Unit {
                if (xmlHttp.readyState as Int == 4) {
                    if (xmlHttp.status as Int == 200) {
                        collectSecurityToken(xmlHttp)
                        successCallback()
                    } else {
                        if (((xmlHttp.status as Int) >= 500) && ((xmlHttp.status as Int) < 600)) {
                            errorNotification.set("Server error.")
                        }
                        if (xmlHttp.status as Int == 401) {
                            context.clear()
                            return
                        }
                        val errorMessage = "Response code ${xmlHttp.status} for REST API DELETE at $url."
                        errorCallback(errorMessage)
                    }
                }
                return
            }
            if (value != null) {
                xmlHttp.send(toJson(value))
            } else {
                xmlHttp.send()
            }
        })
    }

    private fun setAuthorizationHeader(xmlHttp: XMLHttpRequest) {
        if (username != null && password != null) {
            xmlHttp.setRequestHeader("Authorization", "Basic ${base64Encode("$username:$password")}")
        } else if (securityToken != null) {
            xmlHttp.setRequestHeader("Authorization", "SecurityToken token='${encodeURIComponent(securityToken!!)}'")
        }
    }

    fun collectSecurityToken(xmlHttp: XMLHttpRequest) {
        if (xmlHttp.getResponseHeader("Security-Token") != null) {
            securityToken = xmlHttp.getResponseHeader("Security-Token")
            sessionStorage["Security-Token"] = securityToken!!
            println("Received security token.")
            username = null
            password = null
        }
    }
}