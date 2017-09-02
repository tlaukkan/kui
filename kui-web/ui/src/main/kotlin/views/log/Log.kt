package views.log

data class Log(
        var host: String? = null,
        var log: String? = null,
        var created: Long? = null,
        var modified: Long? = null
        )