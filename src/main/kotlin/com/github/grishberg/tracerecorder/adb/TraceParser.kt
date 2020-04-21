package com.github.grishberg.tracerecorder.adb

import com.android.ddmlib.MultiLineReceiver
import com.github.grishberg.tracerecorder.SystraceRecord
import java.util.*


private const val BEGIN_TRACE =
    "\\s+\\S+\\s\\[(\\S+)\\]\\s\\S+\\s(\\d+\\.\\d+):\\s\\w+\\:\\sB\\|\\d+\\|(.+)"
private const val END_TRACE = "\\s+\\S+\\s\\[(\\S+)\\]\\s\\S+\\s(\\d+\\.\\d+):\\s\\w+\\:\\sE"

/**
 * Systrace parser.
 */
class TraceParser : MultiLineReceiver() {
    private val records = Stack<SystraceRecord>()
    private val _values = mutableListOf<SystraceRecord>()

    val values: List<SystraceRecord>
        get() = _values.toList()

    override fun processNewLines(lines: Array<out String>) {
        records.clear()

        if (lines.size <= 1) {
            return
        }
        val beginTracePattern = BEGIN_TRACE.toRegex()
        val endTracePattern = END_TRACE.toRegex()

        for (line in lines) {
            if (line.startsWith("TRACE:")) {
                continue
            }

            if (line.startsWith("#")) {
                continue
            }

            val beginResult = beginTracePattern.find(line)

            if (beginResult != null) {
                val cpu = beginResult.groupValues[1]
                val timestamp = beginResult.groupValues[2].toDouble()
                val name = beginResult.groupValues[3]
                val record = SystraceRecord(name, cpu, timestamp)
                records.push(record)
                _values.add(record)
                continue
            }

            val endResult = endTracePattern.find(line)

            if (endResult != null) {
                val timestamp = endResult.groupValues[2].toDouble()
                val record = records.pop()
                record?.endTime = timestamp
            }
        }
    }

    override fun isCancelled() = false
}