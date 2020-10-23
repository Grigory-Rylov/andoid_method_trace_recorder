package com.github.grishberg.tracerecorder.adb

import com.android.ddmlib.MultiLineReceiver
import com.github.grishberg.tracerecorder.SystraceRecord
import com.github.grishberg.tracerecorder.common.RecorderLogger
import java.util.*


private const val BEGIN_TRACE = "\\s(\\d+\\.\\d+):\\s\\S+\\:\\sB\\|\\d+\\|(.+)"
private const val END_TRACE = "\\s(\\d+\\.\\d+):\\s\\w+\\:\\sE"
private const val PARENT_TS_TRACE = "\\s(\\d+\\.\\d+):\\s\\w+\\:\\strace_event_clock_sync:\\sparent_ts=(\\d+\\.\\d+)"

private const val START_OFFSET_INDEX = 1
private const val TIME_STAMP_INDEX = 1
private const val PARENT_TS_INDEX = 2
private const val NAME_INDEX = 2

/**
 * Systrace parser.
 */
class TraceParser(
    private val logger: RecorderLogger
) : MultiLineReceiver() {
    private val records = Stack<SystraceRecord>()
    private val _values = mutableListOf<SystraceRecord>()

    var startOffset: Double = 0.0
        private set
    var parentTs: Double = 0.0
        private set

    val values: List<SystraceRecord>
        get() = _values.toList()

    override fun processNewLines(lines: Array<out String>) {
        records.clear()

        if (lines.size <= 1) {
            return
        }
        val beginTracePattern = BEGIN_TRACE.toRegex()
        val endTracePattern = END_TRACE.toRegex()
        val parentTsPatter = PARENT_TS_TRACE.toRegex()

        for (line in lines) {
            logger.d(line)
            if (line.startsWith("TRACE:")) {
                continue
            }

            if (line.startsWith("#")) {
                continue
            }

            val offsetResult = parentTsPatter.find(line)
            if (offsetResult != null) {
                startOffset = offsetResult.groupValues[START_OFFSET_INDEX].toDouble()
                parentTs = offsetResult.groupValues[PARENT_TS_INDEX].toDouble()
            }

            val beginResult = beginTracePattern.find(line)

            if (beginResult != null) {
                // TODO: fix regex to find cpu, val cpu = beginResult.groupValues[1]
                val timestamp = beginResult.groupValues[TIME_STAMP_INDEX].toDouble()
                val name = beginResult.groupValues[NAME_INDEX]
                val record = SystraceRecord(name, "-", timestamp)
                records.push(record)
                _values.add(record)
                continue
            }

            val endResult = endTracePattern.find(line)

            if (endResult != null) {
                val timestamp = endResult.groupValues[2].toDouble()
                if (records.isNotEmpty()) {
                    val record = records.pop()
                    record?.endTime = timestamp
                } else {
                    logger.e("Error: there is no values in stack")
                }
            }
        }
    }

    override fun isCancelled() = false
}
