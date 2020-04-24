package com.github.grishberg.tracerecorder.common

class ConsoleLogger : RecorderLogger {
    override fun d(msg: String) {
        println("D: $msg")
    }

    override fun e(msg: String) {
        println("E: $msg")
    }

    override fun e(msg: String, t: Throwable) {
        println("E $msg, ${t.printStackTrace()}")
    }
}