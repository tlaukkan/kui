package views.log

import org.kui.model.LogRow

data class LogResult(var endTime: Long?, var nextBeginId: String?, var rows: Array<LogRow>?)
