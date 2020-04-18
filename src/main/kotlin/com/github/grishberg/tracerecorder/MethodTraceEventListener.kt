package com.github.grishberg.tracerecorder

import java.io.File

interface MethodTraceEventListener {
    fun success(traceFile: File)

    fun onSuccessRemote(remoteFilePath: String)

    fun fail(throwable: Throwable)
}