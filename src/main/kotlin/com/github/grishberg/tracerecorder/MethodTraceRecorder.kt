package com.github.grishberg.tracerecorder

import com.github.grishberg.tracerecorder.exceptions.MethodTraceRecordException

interface MethodTraceRecorder {
    /**
     * Starts method trace recording.
     *
     * @param startActivityName activity for starting application.
     * If [startActivityName] not given - should start application manually
     * @param samplingIntervalInMicroseconds interval in microseconds.
     */
    @Throws(MethodTraceRecordException::class)
    fun startRecording(
        outputFileName: String,
        packageName: String,
        startActivityName: String?,
        samplingIntervalInMicroseconds: Int
    )

    /**
     * Same as method above but samplingInterval is 1
     */
    @Throws(MethodTraceRecordException::class)
    fun startRecording(outputFileName: String, packageName: String, startActivityName: String?)

    /**
     * Stops recoding.
     */
    fun stopRecording()

    /**
     * Force disconnect adb.
     */
    fun disconnect()
}