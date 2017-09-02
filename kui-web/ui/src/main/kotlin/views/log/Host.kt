package views.log

data class Host (
        var environmentType: String? = null,
        var environment: String? = null,
        var hostType: String? = null,
        var host: String? = null,
        var owner: String? = null,
        var created: Long? = null,
        var modified: Long? = null
        )