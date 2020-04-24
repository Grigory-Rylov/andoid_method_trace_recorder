package com.github.grishberg.tracerecorder.common

interface RecorderLogger {
    fun d(msg: String)
    fun e(msg: String)
    fun e(msg: String, t: Throwable)
}