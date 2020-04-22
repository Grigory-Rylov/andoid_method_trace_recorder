package com.github.grishberg.tracerecorder.adb

import com.android.ddmlib.MultiLineReceiver
import com.github.grishberg.tracerecorder.common.RecorderLogger

class ShellOutputReceiver(
    private val log: RecorderLogger
) : MultiLineReceiver() {
    override fun processNewLines(lines: Array<out String>) {
        for (line in lines) {
            log.d(line)
        }
    }

    override fun isCancelled(): Boolean {
        return false
    }
}