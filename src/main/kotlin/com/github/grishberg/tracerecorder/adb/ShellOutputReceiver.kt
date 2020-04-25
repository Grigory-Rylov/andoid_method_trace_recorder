package com.github.grishberg.tracerecorder.adb

import com.android.ddmlib.MultiLineReceiver
import com.github.grishberg.tracerecorder.MethodTraceEventListener
import com.github.grishberg.tracerecorder.common.RecorderLogger
import com.github.grishberg.tracerecorder.exceptions.StartActivityException

private const val ERROR_PREFIX = "Error: "

class ShellOutputReceiver(
    private val log: RecorderLogger,
    private val listener: MethodTraceEventListener
) : MultiLineReceiver() {
    private var state: State = Idle()
    private var isCancelled = false

    override fun processNewLines(lines: Array<out String>) {
        for (line in lines) {
            log.d(line)
            state.processLine(line)
        }
    }

    override fun isCancelled(): Boolean {
        return isCancelled
    }

    private inner class Error : State {
        override fun processLine(line: String) {
            val errorPos = line.indexOf(ERROR_PREFIX)
            if (errorPos >= 0) {
                listener.fail(StartActivityException(line.substring(errorPos + ERROR_PREFIX.length)))
                isCancelled = true
            }
        }
    }

    private inner class Idle : State {
        override fun processLine(line: String) {
            if (line.contains("Error type")) {
                state = Error()
            }
        }
    }

    private interface State {
        fun processLine(line: String)
    }
}