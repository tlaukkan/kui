package views.log

/**
 * Created by tlaukkan on 6/29/2017.
 */
data class Line(var id: String? = null, var log_id: String? = null, val content: String, var created: Long?, var received: Long? = null)
