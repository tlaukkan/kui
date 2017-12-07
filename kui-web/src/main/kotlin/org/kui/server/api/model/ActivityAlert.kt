package views.alerts.activity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.kui.security.model.Record
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
data class ActivityAlert(
        override var key: String? = null,
        override var created: Date? = null,
        override var modified: Date? = null,
        var environment: String? = null,
        var hostType: String? = null,
        var host: String? = null,
        var log: String? = null,
        var tag: String? = null,
        var period: Long? = null,
        var min: Int? = null,
        var max: Int? = null,
        var email: String? = null,
        var workUnitCreated: Boolean = false,
        var checkedSince: Date? = null,
        var checkedUntil: Date? = null) : Record
