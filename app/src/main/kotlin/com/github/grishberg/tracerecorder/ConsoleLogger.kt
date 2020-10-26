package com.github.grishberg.tracerecorder

import com.github.grishberg.android.adb.AdbLogger

class ConsoleLogger : AdbLogger {
    override fun d(msg: String) {
        println("D: $msg")
    }

    override fun e(msg: String) {
        println("E: $msg")
    }

    override fun e(msg: String, t: Throwable) {
        println("E $msg, ${t.printStackTrace()}")
    }

    override fun w(msg: String) {
        println("W $msg")
    }
}
