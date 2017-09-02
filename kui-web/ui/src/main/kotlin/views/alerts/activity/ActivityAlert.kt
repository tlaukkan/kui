package views.alerts.activity

data class ActivityAlert(
        var key: String? = null,
        var created: Long? = null,
        var modified: Long? = null,
        var environment: String? = null,
        var hostType: String? = null,
        var host: String? = null,
        var log: String? = null,
        var tag: String? = null,
        var period: Int? = null,
        var min: Int? = null,
        var max: Int? = null,
        var email: String? = null)
