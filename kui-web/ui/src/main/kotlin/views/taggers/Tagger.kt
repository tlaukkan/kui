package views.taggers

data class Tagger (
        var key: String? = null,
        var created: Long? = null,
        var modified: Long? = null,
        var environment: String? = null,
        var host: String? = null,
        var log: String? = null,
        var pattern: String? = null,
        var tag: String? = null,
        var color: String? = null)