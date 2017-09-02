package org.kui.agent

class AgentException : RuntimeException {

    constructor(message: String?, exception: Exception) : super(message, exception)

    constructor(message: String?) : super(message)
}