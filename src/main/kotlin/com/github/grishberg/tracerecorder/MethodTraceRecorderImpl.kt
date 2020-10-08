package com.github.grishberg.tracerecorder

import com.android.ddmlib.*
import com.github.grishberg.tracerecorder.adb.AdbWrapper
import com.github.grishberg.tracerecorder.adb.AdbWrapperImpl
import com.github.grishberg.tracerecorder.adb.ShellOutputReceiver
import com.github.grishberg.tracerecorder.adb.TraceParser
import com.github.grishberg.tracerecorder.common.NoOpLogger
import com.github.grishberg.tracerecorder.common.RecorderLogger
import com.github.grishberg.tracerecorder.exceptions.*
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.Socket
import java.net.SocketException
import java.util.concurrent.TimeUnit

/**
 * Used for recording method trace file .trace
 * which can be opened in https://github.com/Grigory-Rylov/android-method-trace-viewer-release or Android Studio
 *
 * @param packageName recorded application package.
 * @param outputFileName path to output filename
 */
private const val TAG = "MethodTraceRecorderImpl"


class MethodTraceRecorderImpl(
    private val listener: MethodTraceEventListener,
    private val methodTrace: Boolean,
    private val systrace: Boolean,
    private val logger: RecorderLogger = NoOpLogger(),
    androidHome: String? = null,
    private val debugPort: Int = 8699,
    forceNewBridge: Boolean = false,
    private val waitForDeviceTimeoutInSeconds: Int = 60,
    private val applicationWaitPostTimeoutInMilliseconds: Long = 10
) : MethodTraceRecorder {
    private var client: Client? = null
    private val adb = AdbWrapperImpl(methodTrace, logger, androidHome, forceNewBridge)
    private var measureStrategy: MeasureStrategy = SamplingMeasure()

    @Volatile
    private var shouldRun: Boolean = false

    init {
        MonitorThreadLoggerBridge.setLogger(logger);
        DdmPreferences.setSelectedDebugPort(debugPort)
    }

    @Throws(MethodTraceRecordException::class)
    override fun startRecording(
        outputFileName: String,
        packageName: String,
        startActivityName: String?,
        mode: RecordMode,
        samplingIntervalInMicroseconds: Int,
        profilerBufferSizeInMb: Int,
        waitForApplicationTimeoutInSeconds: Int,
        remoteDeviceAddress: String?
    ) {
        measureStrategy = if (mode == RecordMode.METHOD_SAMPLE) SamplingMeasure() else TracerRecording()

        logger.d("$TAG: startRecording methodTrace=$methodTrace, systrace=$systrace")
        initBaseDebugPort()

        if (methodTrace && isPortAlreadyUsed(DdmPreferences.getSelectedDebugPort())) {
            throw DebugPortBusyException(DdmPreferences.getSelectedDebugPort())
        }
        DdmPreferences.setProfilerBufferSizeMb(profilerBufferSizeInMb)

        if (remoteDeviceAddress != null) {
            adb.connect(remoteDeviceAddress)
        } else {
            adb.connect()
        }
        shouldRun = true

        listener.onStartWaitingForDevice()
        waitForDevices(adb)
        if (!shouldRun) {
            return
        }

        logger.d("$TAG: fetching devices")
        val devices = fetchDevices()
        logger.d("$TAG: found devices: $devices")
        val device = devices.first()

        if (systrace) {
            startTrace(packageName, device)
        }
        if (startActivityName != null) {
            startActivity(packageName, startActivityName, device)
        }

        if (!methodTrace) {
            return
        }

        listener.onStartWaitingForApplication()
        waitForApplication(adb, device, packageName, waitForApplicationTimeoutInSeconds)
        recordSamplingProfile(device, packageName, outputFileName, samplingIntervalInMicroseconds)
    }

    @Throws(AppTimeoutException::class)
    private fun waitForApplication(adb: AdbWrapper, device: IDevice, packageName: String, timeoutInSeconds: Int) {
        logger.d("$TAG: waitForApplication pkg=$packageName, device=$device")
        val startTime = System.currentTimeMillis()
        var count = 0
        while (device.getClient(packageName) == null && shouldRun) {
            val elapsedTimeInMs = System.currentTimeMillis() - startTime
            count++
            if (elapsedTimeInMs > timeoutInSeconds * 1000) {
                adb.stop()
                throw AppTimeoutException(packageName)
            }
        }
        Thread.sleep(applicationWaitPostTimeoutInMilliseconds)
    }

    private fun recordSamplingProfile(
        device: IDevice,
        packageName: String,
        outputFileName: String,
        samplingIntervalInMicroseconds: Int
    ) {
        if (!shouldRun) {
            return
        }

        client = device.getClient(packageName)
        logger.d("$TAG: got client $client")

        ClientData.setMethodProfilingHandler(MethodProfilingHandler(logger, listener, outputFileName, adb))

        logger.d("$TAG: startSamplingProfiler client=$client, interval=$samplingIntervalInMicroseconds")
        measureStrategy.startRecording(samplingIntervalInMicroseconds)
        listener.onStartedRecording()
    }

    private fun initBaseDebugPort() {
        //find at least 10 opened ports
        var openedPortsCount = 0

        for (port in 8600 until 8698) {
            if (isPortAlreadyUsed(port)) {
                openedPortsCount = 0
                logger.d("$TAG: debug port base $port is busy")
                continue
            }

            if (openedPortsCount >= 10) {
                val firstOpenedPort = port - openedPortsCount
                logger.d("$TAG: selected debug port base $firstOpenedPort")
                DdmPreferences.setDebugPortBase(firstOpenedPort)
                return
            }
            openedPortsCount++
        }
    }

    private fun startActivity(packageName: String, mainActivity: String, device: IDevice) {
        val command =
            "am start $packageName/$mainActivity -c android.intent.category.LAUNCHER -a android.intent.action.MAIN"
        logger.d("$TAG: startActivity cmd='$command', device=$device")
        val outputReceiver = ShellOutputReceiver(logger)
        device.executeShellCommand(command, outputReceiver)
        if (outputReceiver.lastException != null) {
            shouldRun = false
            logger.d("$TAG: startActivity cmd='$command', device=$device ended with exception ${outputReceiver.lastException}")
        }
    }

    private fun startTrace(packageName: String, device: IDevice) {
        val command = "atrace -a $packageName -n --async_start"
        logger.d("$TAG: startTrace pkg='$packageName', device=$device")
        device.executeShellCommand(command, ShellOutputReceiver(logger))
    }

    /**
     * Stops recoding.
     */
    override fun stopRecording() {
        logger.d("$TAG stopRecording, methodTrace=$methodTrace")
        shouldRun = false
        if (methodTrace) {
            measureStrategy.stopRecording()
        }

        if (!systrace) {
            return
        }

        val devices = fetchDevices()
        val device = devices.first()

        stopTrace(device)

    }

    private fun fetchDevices(): List<IDevice> {
        val devices = adb.getDevices()
        if (devices.size > 1) {
            throw MethodTraceRecordException("more than one device")
        } else if (devices.isEmpty()) {
            throw NoDeviceException()
        }
        return devices
    }

    private fun stopTrace(device: IDevice) {
        logger.d("$TAG stopTrace, device=$device")
        val command = "atrace --async_stop"
        val traceParser = TraceParser(logger)
        device.executeShellCommand("atrace --async_dump", traceParser)
        device.executeShellCommand(command, TraceParser(NoOpLogger()))
        val values = traceParser.values
        listener.onSystraceReceived(values)
    }

    /**
     * Force disconnect adb.
     */
    override fun disconnect() {
        logger.d("$TAG disconnect")
        shouldRun = false
        adb.stop()
    }

    override fun reconnect(remoteDeviceAddress: String?) {
        if (remoteDeviceAddress != null) {
            adb.connect(remoteDeviceAddress)
        } else {
            adb.connect()
        }
        adb.stop()
    }

    @Throws(DeviceTimeoutException::class)
    private fun waitForDevices(adb: AdbWrapper) {
        logger.d("$TAG: waitForDevice")
        var count = 0
        while (!adb.hasInitialDeviceList() && shouldRun) {
            try {
                Thread.sleep(100)
                count++
            } catch (ignored: InterruptedException) {
            }
            if (count > waitForDeviceTimeoutInSeconds * 10) {
                adb.stop()
                throw DeviceTimeoutException()
            }
        }
    }

    private fun isPortAlreadyUsed(port: Int): Boolean {
        // Assume no connection is possible.
        try {
            (Socket("localhost", port)).close()
            return true
        } catch (e: SocketException) {
            // Could not connect.
            return false
        }
    }

    class MethodProfilingHandler(
        private val logger: RecorderLogger,
        private val listener: MethodTraceEventListener,
        private val outputFileName: String,
        private val adb: AdbWrapper
    ) : ClientData.IMethodProfilingHandler {
        override fun onSuccess(remoteFilePath: String, client: Client) {
            logger.d("$TAG: onSuccess: $remoteFilePath $client")
            listener.onMethodTraceReceived(remoteFilePath)
        }

        override fun onSuccess(data: ByteArray, client: Client) {

            var bs: BufferedOutputStream? = null
            val file = File(outputFileName)

            try {
                val fs = FileOutputStream(file)
                bs = BufferedOutputStream(fs)
                bs.write(data)
                bs.close()
                bs = null
            } catch (e: java.lang.Exception) {
                logger.e("$TAG: save trace file failed", e)
                e.printStackTrace()
            }

            if (bs != null) try {
                bs.close()
            } catch (e: Exception) {
            }

            listener.onMethodTraceReceived(file)
        }

        override fun onStartFailure(client: Client, message: String) {
            adb.stop()
            listener.fail(StartFailureException("onStartFailure: $client $message"))
        }

        override fun onEndFailure(client: Client, message: String) {
            adb.stop()
            listener.fail(EndFailureException("onEndFailure: $client $message"))
        }
    }

    private inner class SamplingMeasure : MeasureStrategy {
        override fun startRecording(interval: Int) {
            client?.startSamplingProfiler(interval, TimeUnit.MICROSECONDS)
        }

        override fun stopRecording() {
            client?.stopSamplingProfiler()
        }
    }

    private inner class TracerRecording : MeasureStrategy {
        override fun startRecording(interval: Int) {
            client?.startMethodTracer()
        }

        override fun stopRecording() {
            client?.stopMethodTracer()
        }
    }

    private interface MeasureStrategy {
        fun startRecording(interval: Int)
        fun stopRecording()
    }
}
