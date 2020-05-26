package com.github.grishberg.tracerecorder.common

interface RecorderLogger {
    fun d(msg: String)
    fun e(msg: String)
    fun e(msg: String, t: Throwable)
    fun w(msg: String)

    fun w(tag: String, msg: String) = w("$tag: $msg")
    fun d(tag: String, msg: String) = d("$tag: $msg")
    fun e(tag: String, msg: String, t: Throwable) = e("$tag: $msg", t)
    fun e(tag: String, msg: String) = e("$tag: $msg")
}
