package org.kui.client

class ClientException : RuntimeException {

    constructor(message: String?, exception: Exception) : super(message, exception)

    constructor(message: String?) : super(message)
}