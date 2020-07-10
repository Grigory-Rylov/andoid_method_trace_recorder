package com.github.grishberg.tracerecorder

import com.github.grishberg.tracerecorder.exceptions.MethodTraceRecordException

/**
 * Record mode. https://developer.android.com/studio/profile/cpu-profiler#configurations
 */
enum class RecordMode {
    /**
     * Sample Java Methods: Captures your app’s call stack at frequent intervals during your app’s
     * Java-based code execution. The profiler compares sets of captured data to derive timing and
     * resource usage information about your app’s Java-based code execution.
     * An inherent issue of sampled-based tracing is that if your app enters a method after a
     * capture of the call stack and exits the method before the next capture, that method call
     * is not logged by the profiler. If you are interested in tracing methods with such short lifecycles,
     * you should use instrumented tracing.
     */
    METHOD_SAMPLE,

    /**
     * Trace Java Methods: Instruments your app at runtime to record a timestamp at the beginning and
     * end of each method call. Timestamps are collected and compared to generate method tracing data,
     * including timing information and CPU usage. Note that the overhead associated with instrumenting
     * each method impacts runtime performance and may influence profiling data; this is even more
     * noticeable for methods with relatively short lifecycles. Additionally, if your app executes a
     * large number of methods in a short time, the profiler may quickly exceed its file size limit
     * and may not be able to record any further tracing data.
     */
    METHOD_TRACES
}

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
        mode: RecordMode,
        samplingIntervalInMicroseconds: Int = 60,
        profilerBufferSizeInMb: Int = 40
    )

    /**
     * Stops recoding.
     */
    fun stopRecording()

    /**
     * Force disconnect adb.
     */
    fun disconnect()

    /**
     * Connect to adb and disconnect immediately.
     */
    fun reconnect()
}
