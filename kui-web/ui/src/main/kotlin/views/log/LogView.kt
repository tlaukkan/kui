package views.log

import api
import components.ViewComponent
import org.w3c.dom.*
import util.*
import kotlin.browser.document
import kotlin.browser.sessionStorage
import kotlin.browser.window
import kotlin.dom.addClass
import kotlin.dom.removeClass
import kotlin.js.Date

private val VIEW_TEMPLATE_PATH = "views/log/LogView.html"

class LogView : ViewComponent(VIEW_TEMPLATE_PATH) {
    var searchButton: HTMLButtonElement? = null
    var logTableContainer: HTMLDivElement? = null
    var graphCanvas: HTMLCanvasElement? = null
    var graphRenderingContext: CanvasRenderingContext2D? = null

    var hostRecords: Array<Host> = arrayOf()
    var logRecords: Array<Log> = arrayOf()

    var selectedEnvironments: MutableSet<String> = mutableSetOf()
    var selectedHosts: MutableSet<String> = mutableSetOf()
    var selectedLogs: MutableSet<String> = mutableSetOf()
    var searchPattern: String = ""

    var liveLoad: Boolean = false

    var rowsLoadedWithoutScroll = 0

    override fun bind() {
        super.bind()

        logTableContainer = document.getElementById("logTableContainer") as HTMLDivElement

        logTableContainer!!.setAttribute("style","height:${window.innerHeight - 250}px")

        searchButton = document.getElementById("searchButton") as HTMLButtonElement
        val patternInput = document.getElementById("patternInput")!! as HTMLInputElement
        patternInput!!.onkeyup = { event ->
            val dynamicEvent: dynamic = event
            if (dynamicEvent.which == 13) {
                searchPatternSet()
                search()
            }
            true
        }

        graphCanvas = document.getElementById("graphCanvas") as HTMLCanvasElement
        graphCanvas!!.width = window.innerWidth - 30

        graphCanvas!!.onclick = { event ->
            var dynamicEvent: dynamic = event
            var rect = graphCanvas!!.getBoundingClientRect()

            var x = (dynamicEvent.clientX - rect.left).toString().toDouble()
            var y = (dynamicEvent.clientY - rect.top).toString().toDouble()

            var slot = x.toInt()
            var selectedRowIndex = rowIndexes[slot]

            val selectedTimeMillis = periodMillis * x / graphCanvas!!.width + beginTimeMillis

            //println("x: " + x + " y: " + y)
            //println("row index: " + selectedRowIndex)

            if (selectedRowIndex == -1) {
                logTableContainer!!.scrollTop = logTableContainer!!.scrollHeight.toDouble()
            } else {
                logTableContainer!!.scrollTop = (logTableContainer!!.scrollHeight * selectedRowIndex / rowCount).toDouble()
            }
        }

        graphRenderingContext = graphCanvas!!.getContext("2d") as CanvasRenderingContext2D

        graphRenderingContext!!.fillStyle = "#FFFFFF"
        graphRenderingContext!!.fillRect(0.0, 0.0, graphCanvas!!.width.toDouble(), graphCanvas!!.height.toDouble())
        graphRenderingContext!!.fillStyle = "#EEEEEE"
        graphRenderingContext!!.fillRect(0.0, 50.0, graphCanvas!!.width.toDouble(), 1.0)


        window.onresize = {
            logTableContainer!!.setAttribute("style","height:${window.innerHeight - 250}px")
            graphCanvas!!.width = window.innerWidth - 30

            graphRenderingContext!!.fillStyle = "#FFFFFF"
            graphRenderingContext!!.fillRect(0.0, 0.0, graphCanvas!!.width.toDouble(), graphCanvas!!.height.toDouble())
            graphRenderingContext!!.fillStyle = "#EEEEEE"
            graphRenderingContext!!.fillRect(0.0, 50.0, graphCanvas!!.width.toDouble(), 1.0)

            true
        }


        api.get<Array<Host>>("log/hosts").error { message: String ->
            println("Error: $message")
        }.success { hostRecords: Array<Host> ->
            this.hostRecords = hostRecords

            api.get<Array<Log>>("log/logs").error { message: String ->
                println("Error: $message")
            }.success { logRecords: Array<Log> ->
                this.logRecords = logRecords
                refreshLogScopeInputs()
                refreshTimeScopeInputs()
                refreshSearchPattern()
                search()
            }
        }
    }

    override fun unbind() {
        unbindLoadMoreFromScroll()
        window.onresize = {}
    }

    fun refreshLogScopeInputs() {
        getRepeater("environmentScope").clearRepeats()
        getRepeater("hostScope").clearRepeats()
        getRepeater("logScope").clearRepeats()

        val url = window.location.href

        val parameters = getParameters(window.location.href)

        var environmentScope = parameters.get("environmentScope")
        var hostScope = parameters.get("hostScope")
        var logScope = parameters.get("logScope")

        if (liveLoad) {
            today()
        }

        if (environmentScope.isNullOrBlank() && !sessionStorage.getItem("environmentScope").isNullOrBlank()) {
            environmentScope = sessionStorage.getItem("environmentScope")
            pushLocationState(setParameter(window.location.href, "environmentScope", environmentScope!!))
        }
        if (hostScope.isNullOrBlank() && !sessionStorage.getItem("hostScope").isNullOrBlank()) {
            hostScope = sessionStorage.getItem("hostScope")
            pushLocationState(setParameter(window.location.href, "hostScope", hostScope!!))
        }
        if (logScope.isNullOrBlank() && !sessionStorage.getItem("logScope").isNullOrBlank()) {
            logScope = sessionStorage.getItem("logScope")
            pushLocationState(setParameter(window.location.href, "logScope", logScope!!))
        }

        val environments = mutableSetOf<String>()
        val environmentTypes = mutableSetOf<String>()
        val hostTypes = mutableSetOf<String>()
        val hosts = mutableSetOf<String>()

        for (hostRecord in hostRecords) {
            if (hostRecord.environment != null && !environments.contains(hostRecord.environment!!)) {
                environments.add(hostRecord.environment!!)
            }
            if (hostRecord.environmentType != null && !environmentTypes.contains(hostRecord.environmentType!!)) {
                environmentTypes.add(hostRecord.environmentType!!)
            }

            if (environmentScope != null) {
                if (environmentScope.equals(hostRecord.environment)) {
                    if (hostRecord.hostType != null) {
                        hostTypes.add(hostRecord.hostType!!)
                    }
                    hosts.add(hostRecord.host!!)
                }
                if (environmentScope.equals(hostRecord.environmentType)) {
                    if (hostRecord.hostType != null) {
                        hostTypes.add(hostRecord.hostType!!)
                    }
                    hosts.add(hostRecord.host!!)
                }
            } else {
                if (hostRecord.hostType != null) {
                    hostTypes.add(hostRecord.hostType!!)
                }
                hosts.add(hostRecord.host!!)
            }
        }

        if (environmentScope != null) {
            if (!environmentTypes.contains(environmentScope) && !environments.contains(environmentScope)) {
                environmentScope = null
                pushLocationState(setParameter(window.location.href, "environmentScope", ""))
            }
        }

        if (environmentScope != null) {
            document.getElementById("environmentScopeDrop")!!.innerHTML = environmentScope!!
        } else {
            document.getElementById("environmentScopeDrop")!!.innerHTML = "Environment"
        }

        if (hostScope != null) {
            if (!hostTypes.contains(hostScope) && !hosts.contains(hostScope)) {
                hostScope = null
                pushLocationState(setParameter(window.location.href, "hostScope", ""))
            }
        }

        if (hostScope != null) {
            document.getElementById("hostScopeDrop")!!.innerHTML = hostScope!!
        } else {
            document.getElementById("hostScopeDrop")!!.innerHTML = "Host"
        }

        getRepeater("environmentScope").repeat(mapOf("url" to setParameter(url, "environmentScope", ""), "environmentScope" to "Clear"))
        getRepeater("environmentScope").repeatHtml("<div class=\"dropdown-divider\"></div>")
        for (environmentType in environmentTypes) {
            getRepeater("environmentScope").repeat(mapOf("environmentScope" to environmentType))
        }
        getRepeater("environmentScope").repeatHtml("<div class=\"dropdown-divider\"></div>")
        for (environment in environments) {
            getRepeater("environmentScope").repeat(mapOf("environmentScope" to environment))
        }

        getRepeater("hostScope").repeat(mapOf("url" to setParameter(url, "hostScope", ""), "hostScope" to "All"))
        getRepeater("hostScope").repeatHtml("<div class=\"dropdown-divider\"></div>")
        for (hostType in hostTypes) {
            getRepeater("hostScope").repeat(mapOf("hostScope" to hostType))
        }
        getRepeater("hostScope").repeatHtml("<div class=\"dropdown-divider\"></div>")
        for (host in hosts) {
            getRepeater("hostScope").repeat(mapOf("hostScope" to host!!))
        }


        selectedHosts.clear()
        selectedEnvironments.clear()

        val hostsInScope = mutableListOf<String>()
        for (host in hostRecords) {
            if (hostScope != null) {
                if (hostScope.equals(host.hostType)) {
                    selectedHosts.add(host.host!!)
                    hostsInScope.add(host.host!!)
                } else if (hostScope.equals(host.host)) {
                    selectedHosts.add(host.host!!)
                    hostsInScope.add(host.host!!)
                }
            } else if (environmentScope != null) {
                if (environmentScope.equals(host.environmentType)) {
                    selectedEnvironments.add(host.environment!!)
                    hostsInScope.add(host.host!!)
                }  else if (environmentScope.equals(host.environment)) {
                    selectedEnvironments.add(host.environment!!)
                    hostsInScope.add(host.host!!)
                }
            }
        }

        val logs = mutableSetOf<String>()
        for (log in logRecords) {
            if (!hostsInScope.contains(log.host)) {
                continue
            }
            if (!logs.contains(log.log!!)) {
                logs.add(log.log!!)
            }
        }

        getRepeater("logScope").repeat(mapOf("logScopeLabel" to "Clear", "logScope" to ""))
        getRepeater("logScope").repeatHtml("<div class=\"dropdown-divider\"></div>")
        for (log in logs) {
            getRepeater("logScope").repeat(mapOf("logScopeLabel" to log!!,"logScope" to log!!.replace("\\", "\\\\")))
        }

        if (logScope != null) {
            if (!logs.contains(logScope!!)) {
                logScope = null
                pushLocationState(setParameter(window.location.href, "logScope", ""))
            }
        }
        if (logScope != null) {
            selectedLogs.clear()
            selectedLogs.add(logScope)
            document.getElementById("logScopeDrop")!!.innerHTML = logScope!!
        } else {
            document.getElementById("logScopeDrop")!!.innerHTML = "Log"
        }

        //search()
    }


    fun refreshTimeScopeInputs() {
        var parameters = getParameters(window.location.href)
        if (!parameters.containsKey("timeScope") || parameters["timeScope"]!!.size == 0) {
            if (!sessionStorage.getItem("timeScope").isNullOrBlank()) {
                pushLocationState(setParameter(window.location.href, "timeScope", sessionStorage.getItem("timeScope")!!))
                parameters = getParameters(window.location.href)
            } else {
                pushLocationState(setParameter(window.location.href, "timeScope", "today"))
                parameters = getParameters(window.location.href)
            }
        }

        val liveButton = document.getElementById("liveButton")!!
        val todayButton = document.getElementById("todayButton")!!
        val yesterdayButton = document.getElementById("yesterdayButton")!!
        val lastWeekButton = document.getElementById("lastWeekButton")!!
        val lastMonthButton = document.getElementById("lastMonthButton")!!

        val sinceInput = document.getElementById("sinceInput")!! as HTMLInputElement
        val untilInput = document.getElementById("untilInput")!! as HTMLInputElement

        if (parameters.containsKey("timeScope")) {
            val timeScope = parameters["timeScope"]
            if (timeScope.equals("live")) {
                liveButton.addClass("active")
                val todayBegin = getDatePart(Date())
                sinceInput.value = dateToUiString(addMinutes(Date(), -15))
                untilInput.value = dateToUiString(addDays(todayBegin, 1))
                liveLoad = true
            } else {
                liveButton.removeClass("active")
                liveLoad = false
            }
            if (timeScope.equals("today")) {
                todayButton.addClass("active")
                val todayBegin = getDatePart(Date())
                sinceInput.value = dateToUiString(addDays(todayBegin, 0))
                untilInput.value = dateToUiString(addDays(todayBegin, 1))
            } else {
                todayButton.removeClass("active")
            }
            if (timeScope.equals("yesterday")) {
                yesterdayButton.addClass("active")
                val todayBegin = getDatePart(Date())
                sinceInput.value = dateToUiString(addDays(todayBegin, -1))
                untilInput.value = dateToUiString(addDays(todayBegin, 0))
            } else {
                yesterdayButton.removeClass("active")
            }
            if (timeScope.equals("lastWeek")) {
                lastWeekButton.addClass("active")
                val todayBegin = getDatePart(Date())
                sinceInput.value = dateToUiString(addDays(todayBegin, -7))
                untilInput.value = dateToUiString(addDays(todayBegin, 0))
            } else {
                lastWeekButton.removeClass("active")
            }
            if (timeScope.equals("lastMonth")) {
                lastMonthButton.addClass("active")
                val todayBegin = getDatePart(Date())
                sinceInput.value = dateToUiString(addDays(todayBegin, -30))
                untilInput.value = dateToUiString(addDays(todayBegin, 0))
            } else {
                lastMonthButton.removeClass("active")
            }
            if (timeScope!!.contains("/")) {
                val timeScopeParts = timeScope.split('/')
                sinceInput.value = timeScopeParts[0]
                untilInput.value = timeScopeParts[1]
            }
        } else {
            liveLoad = false
            liveButton.removeClass("active")
            todayButton.removeClass("active")
            yesterdayButton.removeClass("active")
            lastWeekButton.removeClass("active")
            lastMonthButton.removeClass("active")
        }

        if (liveLoad && !searchButton!!.disabled) {
            search()
        }
    }

    fun setCustomTimeScope() {
        pushLocationState(setParameter(window.location.href, "timeScope", ""))

        val sinceInput = document.getElementById("sinceInput")!! as HTMLInputElement
        val untilInput = document.getElementById("untilInput")!! as HTMLInputElement

        pushLocationState(setParameter(window.location.href, "timeScope", sinceInput.value + "/" + untilInput.value))

        refreshTimeScopeInputs()
    }


    @JsName("environmentScopeChange")
    fun environmentScopeChange(environmentScope: String) {
        pushLocationState(setParameter(window.location.href, "environmentScope", environmentScope))
        sessionStorage.setItem("environmentScope", environmentScope)
        refreshLogScopeInputs()
    }

    @JsName("hostScopeChange")
    fun hostScopeChange(hostScope: String) {
        pushLocationState(setParameter(window.location.href, "hostScope", hostScope))
        sessionStorage.setItem("hostScope", hostScope)
        refreshLogScopeInputs()
    }

    @JsName("logScopeChange")
    fun logScopeChange(logScope: String) {
        pushLocationState(setParameter(window.location.href, "logScope", logScope))
        sessionStorage.setItem("logScope", logScope)
        refreshLogScopeInputs()
        search()
    }

    fun live() {
        if (liveLoad && searchButton!!.disabled) {
            today()
            return
        }
        pushLocationState(setParameter(window.location.href, "timeScope", "live"))
        sessionStorage.setItem("timeScope", "live")
        refreshTimeScopeInputs()
    }

    fun today() {
        pushLocationState(setParameter(window.location.href, "timeScope", "today"))
        sessionStorage.setItem("timeScope", "today")
        refreshTimeScopeInputs()
    }

    fun yesterday() {
        pushLocationState(setParameter(window.location.href, "timeScope", "yesterday"))
        sessionStorage.setItem("timeScope", "yesterday")
        refreshTimeScopeInputs()
    }

    fun lastWeek() {
        pushLocationState(setParameter(window.location.href, "timeScope", "lastWeek"))
        sessionStorage.setItem("timeScope", "lastWeek")
        refreshTimeScopeInputs()
    }

    fun lastMonth() {
        pushLocationState(setParameter(window.location.href, "timeScope", "lastMonth"))
        sessionStorage.setItem("timeScope", "lastMonth")
        refreshTimeScopeInputs()
    }

    fun refreshSearchPattern() {
        val patternInput = document.getElementById("patternInput")!! as HTMLInputElement
        var parameters = getParameters(window.location.href)
        if (parameters.containsKey("searchPattern")) {
            searchPattern = parameters["searchPattern"]!!
            patternInput.value = searchPattern
        }

    }

    fun searchPatternSet() {
        val patternInput = document.getElementById("patternInput")!! as HTMLInputElement
        pushLocationState(setParameter(window.location.href, "searchPattern", patternInput.value))
        refreshSearchPattern()
    }

    override fun refresh() {

    }

    var beginTimeMillis: Long = 0
    var endTimeMillis: Long = 0
    var periodMillis: Double = 0.0
    var currentEndTimeMillis: Double = 0.0

    var rowCounters: Array<Double> = Array<Double>(0, { 0.0 })
    var tagCounters: Array<Double> = Array<Double>(0, { 0.0 })

    var rowIndexes: Array<Int> = Array<Int>(0, { -1 })
    var rowCount = 0
    var maxFilledSlot = 0


    fun search() {
        unbindLoadMoreFromScroll()

        getRepeater("row").clearRepeats()

        val sinceInput = document.getElementById("sinceInput")!! as HTMLInputElement
        val untilInput = document.getElementById("untilInput")!! as HTMLInputElement

        val beginTime = encodeURIComponent(sinceInput.value)
        val endTime = encodeURIComponent(untilInput.value)

        // Initialize graph canvas
        graphRenderingContext!!.fillStyle = "#FFFFFF"
        graphRenderingContext!!.fillRect(0.0, 0.0, graphCanvas!!.width.toDouble(), graphCanvas!!.height.toDouble())
        graphRenderingContext!!.fillStyle = "#EEEEEE"
        graphRenderingContext!!.fillRect(0.0, 50.0, graphCanvas!!.width.toDouble(), 1.0)

        beginTimeMillis = dateStringToLongString(sinceInput.value.substring(0, sinceInput.value.indexOf('.'))).toLong()
        endTimeMillis = dateStringToLongString(untilInput.value.substring(0, untilInput.value.indexOf('.'))).toLong()
        periodMillis = (endTimeMillis - beginTimeMillis).toDouble()
        currentEndTimeMillis = 0.0
        rowCounters = Array<Double>(graphCanvas!!.width, { 0.0 })
        tagCounters = Array<Double>(graphCanvas!!.width, { 0.0 })
        rowIndexes = Array<Int>(graphCanvas!!.width, { -1 })
        rowCount = 0
        maxFilledSlot = 0

        if (beginTime.size == 0) {
            return
        }
        if (endTime.size == 0) {
            return
        }
        if (selectedEnvironments.size == 0 && selectedHosts.size == 0) {
            return
        }
        if (selectedLogs.size == 0) {
            return
        }

        val environments = encodeURIComponent(setToString(selectedEnvironments))
        val hosts = encodeURIComponent(setToString(selectedHosts))
        val logs = encodeURIComponent(setToString(selectedLogs))

        searchButton!!.disabled = true
        api.get<LogResult>("log/rows?environments=$environments&hosts=$hosts&logs=$logs&beginTime=${beginTimeMillis}&endTime=${endTimeMillis}&pattern=$searchPattern").error { message: String ->
            println("Error: $message")
        }.success { result ->
            rowsLoadedWithoutScroll = 0
            addRows(result, environments, hosts, logs, beginTimeMillis, endTimeMillis, searchPattern, null)
        }
    }

    fun loadMore(environments: String, hosts: String, logs: String, beginTime: Long, endTime: Long, pattern: String, beginId: String?, waitTimeMillis : Int = 1000) {
        //document.getElementById("logTable")!!.setAttribute("hidden","")
        window.setTimeout({
            loadMoreDelayed(environments, hosts, logs, beginTime, endTime, pattern, beginId)
        }, waitTimeMillis)

    }

    fun loadMoreDelayed(environments: String, hosts: String, logs: String, beginTime: Long, endTime: Long, pattern: String, beginId: String?) {
        val loadRowsStarted = Date()
        api.get<LogResult>("log/rows?environments=$environments&hosts=$hosts&logs=$logs&beginTime=$beginTime&endTime=$endTime&pattern=$pattern&beginId=$beginId").error { message: String ->
            println("Error: $message")
        }.success { result ->
            val addRowsStarted = Date()
            println("load rows took: ${addRowsStarted.getTime() - loadRowsStarted.getTime()} ms.")
            addRows(result, environments, hosts, logs, beginTime, endTime, pattern, beginId)
            println("add rows (total: $rowCount) took: ${Date().getTime() - loadRowsStarted.getTime()} ms.")
            //document.getElementById("logTable")!!.removeAttribute("hidden")
        }
    }

    private fun addRows(result: LogResult, environments: String, hosts: String, logs: String, beginTime: Long, endTime: Long, pattern: String, oldBeginId: String?) {

        if (result.endTime != null || result.rows!!.size > 0) {
            val time: Double
            if (result.endTime != null) {
                time = result.endTime.toString().toDouble()
            } else {
                time = result.rows!!.last().time.toString().toDouble()
            }
            val x = (time - beginTimeMillis) * graphCanvas!!.width / periodMillis
            currentEndTimeMillis = time
            graphRenderingContext!!.fillStyle = "#CCCCCC"
            graphRenderingContext!!.fillRect(0.0, 50.0, x.toInt().toDouble(), 1.0)
        }

        var maxSlot = 0
        val scrollBottom : Boolean = (logTableContainer!!.scrollHeight as Double - logTableContainer!!.scrollTop - logTableContainer!!.clientHeight) < 10
        val repeaterHtmlFragmentBuilder = StringBuilder()
        for (row in result.rows!!) {
            val tags: String
            if (row.tags != null) {
                val tagsBuilder = StringBuilder(" (")
                for (tag in row.tags!!) {
                    if (tagsBuilder.size > 2) {
                        tagsBuilder.append(", ")
                    }
                    tagsBuilder.append("<span style=\"color: ${escapeHtml(tag.color!!)}\">${escapeHtml(tag.tag!!)}</span>")
                    //tagsBuilder.append("${escapeHtml(tag.tag!!.toUpperCase())}")
                }
                tagsBuilder.append(")")
                tags = tagsBuilder.toString()
            } else {
                tags = ""
            }
            repeaterHtmlFragmentBuilder.append(getRepeater("row").repeaterRowHtml(mapOf(
                    "host" to escapeHtml(row.host!!.padStart(20, ' ')),
                    "time" to dateToUiString(getDateFromMilliseconds(row.time!!)).padEnd(20, ' '),
                    "line" to escapeHtml(row.line!!),
                    "tags" to tags
            )))
            repeaterHtmlFragmentBuilder.append("\r\n")

            val timeMillis = getDateFromMilliseconds(row.time!!).getTime()

            val x = (timeMillis - beginTimeMillis) * graphCanvas!!.width / periodMillis
            val slot = x.toInt()

            if (slot > rowCounters.size - 1) {
                continue
            }

            if (!row.line.startsWith('\t')) {
                rowCounters[slot] = rowCounters[slot] + 1
                graphRenderingContext!!.fillStyle = "#AAAAAA"
                graphRenderingContext!!.fillRect(x.toInt().toDouble(), 50 - rowCounters[slot], 1.0, 1.0)
            }

            if (row.tags != null) {
                for (tag in row.tags!!) {
                    tagCounters[slot] = tagCounters[slot] + 1
                    graphRenderingContext!!.fillStyle = tag.color
                    graphRenderingContext!!.fillRect(x.toInt().toDouble() , 50 + tagCounters[slot] , 1.0, 1.0)
                }
            }

            if (rowIndexes[slot] == -1) {
                rowIndexes[slot] = rowCount
            }
            rowCount ++
            maxSlot = slot
        }
        getRepeater("row").repeatAppendFragment(repeaterHtmlFragmentBuilder.toString())

        var lastRowIndex = -1
        for (s in maxSlot downTo maxFilledSlot) {
            if (rowIndexes[s] == -1) {
                rowIndexes[s] = lastRowIndex
            } else {
                lastRowIndex = rowIndexes[s]
            }
        }
        maxFilledSlot = maxSlot

        if (liveLoad && scrollBottom) {
            logTableContainer!!.scrollTop = logTableContainer!!.scrollHeight as Double
        }
        rowsLoadedWithoutScroll += result.rows!!.size
        if (result.nextBeginId != null || liveLoad) {
            if (rowsLoadedWithoutScroll < 100 && result.nextBeginId != null && !liveLoad) {
                //println("Rows loaded without scroll " + rowsLoadedWithoutScroll + ". Loading more...")
                loadMore(environments, hosts, logs, beginTime, endTime, pattern, result.nextBeginId!!)
            } else if (liveLoad) {
                //println("Rows live loaded " + rowsLoadedWithoutScroll + ". Loading more...")
                if (result.nextBeginId != null) {
                    loadMore(environments, hosts, logs, beginTime, endTime, pattern, result.nextBeginId!!)
                } else if (result.rows!!.size > 0) {
                    loadMore(environments, hosts, logs, beginTime, endTime, pattern, result.rows!!.last().id)
                } else {
                    loadMore(environments, hosts, logs, beginTime, endTime, pattern, oldBeginId)
                }
            } else {
                //println("Rows loaded without scroll " + rowsLoadedWithoutScroll + " waiting for scroll.")
                bindLoadMoreOnScroll(environments, hosts, logs, beginTime, endTime, pattern, result.nextBeginId!!)
                searchButton!!.disabled = false
            }
        } else {
            searchButton!!.disabled = false
        }
    }

    private fun bindLoadMoreOnScroll(environments: String, hosts: String, logs: String, beginTime: Long, endTime: Long, pattern: String, nextBeginId: String) {
        rowsLoadedWithoutScroll = 0
        logTableContainer!!.onscroll = { event ->
            if (logTableContainer!!.scrollTop >= logTableContainer!!.scrollHeight - 2 * logTableContainer!!.clientHeight) {
                searchButton!!.disabled = true
                unbindLoadMoreFromScroll()
                loadMore(environments, hosts, logs, beginTime, endTime, pattern, nextBeginId, 0)
            }
        }
    }

    private fun unbindLoadMoreFromScroll() {
        logTableContainer!!.onscroll = { }
    }

    fun pushLocationState(url: String) {
        window.history.pushState(url, document.title, url)
    }

}