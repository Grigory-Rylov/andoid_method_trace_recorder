package com.github.grishberg.tracerecorder

import java.io.File

interface MethodTraceEventListener {
    fun success(traceFile: File)

    fun fail(throwable: Throwable)
}