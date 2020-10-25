package com.github.grishberg.tracerecorder.adb

import com.github.grishberg.android.adb.AdbLogger
import com.github.grishberg.android.adb.MultiLineShellOutReceiver
import com.github.grishberg.tracerecorder.exceptions.StartActivityException

private const val ERROR_PREFIX = "Error: "

class ShellOutputReceiver(
    private val log: AdbLogger
) : MultiLineShellOutReceiver() {
    private var state: State = Idle()
    private var isCancelled = false
    var lastException: Throwable? = null

    override fun processNewLines(lines: Array<String>) {
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
                isCancelled = true
                val ex = StartActivityException(line.substring(errorPos + ERROR_PREFIX.length))
                lastException = ex
                throw ex
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
