package org.kui

/**
 * Exception implementation for KUI application exceptions.
 */
class KuiException : Exception {

    constructor(message: String?, exception: Exception) : super(message, exception)

    constructor(message: String?) : super(message)
}