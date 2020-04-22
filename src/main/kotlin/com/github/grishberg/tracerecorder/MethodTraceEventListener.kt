package com.github.grishberg.tracerecorder

import java.io.File

interface MethodTraceEventListener {
    fun onMethodTraceReceived(traceFile: File)
    fun onMethodTraceReceived(remoteFilePath: String)
    fun onSystraceReceived(values: List<SystraceRecord>)
    fun fail(throwable: Throwable)
}