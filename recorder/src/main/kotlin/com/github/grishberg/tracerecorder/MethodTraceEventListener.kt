package com.github.grishberg.tracerecorder

import java.io.File

interface MethodTraceEventListener {
    fun onStartedRecording()
    fun onStartWaitingForDevice() = Unit
    fun onStartWaitingForApplication() = Unit
    fun onMethodTraceReceived(traceFile: File)
    fun onMethodTraceReceived(remoteFilePath: String)
    fun onSystraceReceived(result: SystraceRecordResult)
    fun fail(throwable: Throwable)
}
