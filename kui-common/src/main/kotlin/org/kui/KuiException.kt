package org.kui

class KuiException : RuntimeException {

    constructor(message: String?, exception: Exception) : super(message, exception)

    constructor(message: String?) : super(message)
}