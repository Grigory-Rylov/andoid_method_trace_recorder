package com.github.grishberg.tracerecorder

import com.github.grishberg.tracerecorder.exceptions.MethodTraceRecordException

interface MethodTraceRecorder {
    /**
     * Starts method trace recording.
     *
     * @param startActivityName activity for starting application.
     * If [startActivityName] not given - should start application manually
     */
    @Throws(MethodTraceRecordException::class)
    fun startRecording(packageName: String, startActivityName: String?)

    /**
     * Stops recoding.
     */
    fun stopRecording()

    /**
     * Force disconnect adb.
     */
    fun disconnect()
}