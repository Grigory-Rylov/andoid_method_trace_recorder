package com.github.grishberg.tracerecorder.adb

import com.android.ddmlib.MultiLineReceiver

class ShellOutputReceiver() : MultiLineReceiver() {
    override fun processNewLines(lines: Array<out String>) {
        for (line in lines) {
            println(line)
        }
    }

    override fun isCancelled(): Boolean {
        return false
    }
}