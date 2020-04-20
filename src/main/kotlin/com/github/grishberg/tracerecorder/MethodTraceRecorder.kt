package com.github.grishberg.tracerecorder

import com.android.ddmlib.Client
import com.android.ddmlib.ClientData
import com.android.ddmlib.DdmPreferences
import com.android.ddmlib.IDevice
import com.github.grishberg.tracerecorder.adb.AdbWrapper
import com.github.grishberg.tracerecorder.adb.AdbWrapperImpl
import com.github.grishberg.tracerecorder.adb.ShellOutputReceiver
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
class MethodTraceRecorder(
    private val outputFileName: String,
    private val listener: MethodTraceEventListener
) {
    private var client: Client? = null
    private var adbWrapper: AdbWrapper? = null
    private var shouldRun: Boolean = false

    /**
     * Starts method trace recording.
     *
     * @param startActivityName activity for starting application.
     * If [startActivityName] not given - should start application manually
     */
    @Throws(MethodTraceRecordException::class)
    fun startRecording(packageName: String, startActivityName: String?) {

        if (isPortAlreadyUsed(DdmPreferences.getSelectedDebugPort())) {
            throw DebugPortBusyException(DdmPreferences.getSelectedDebugPort())
        }

        val adb = AdbWrapperImpl()
        adbWrapper = adb
        shouldRun = true

        waitForDevice(adb)

        val devices = adb.getDevices()
        if (devices.size > 1) {
            throw MethodTraceRecordException("more than one device")
        } else if (devices.isEmpty()) {
            throw NoDeviceException()
        }
        val device = devices.first()

        if (startActivityName != null) {
            startActivity(packageName, startActivityName, device)
        }

        waitForApplication(adb, device, packageName)

        client = device.getClient(packageName)
        ClientData.setMethodProfilingHandler(object : ClientData.IMethodProfilingHandler {
            override fun onSuccess(remoteFilePath: String, client: Client) {
                println("onSuccess: $remoteFilePath $client")
                adb.stop()
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
                    e.printStackTrace()
                }

                if (bs != null) try {
                    bs.close()
                } catch (e: Exception) {
                }

                listener.success(file)
                adb.stop()
            }

            override fun onStartFailure(client: Client, message: String) {
                throw StartFailureException("onStartFailure: $client $message")
                adb.stop()
            }

            override fun onEndFailure(client: Client, message: String) {
                EndFailureException("onEndFailure: $client $message")
                adb.stop()
            }
        })

        client?.startSamplingProfiler(1, TimeUnit.MICROSECONDS)
    }

    private fun startActivity(packageName: String, mainActivity: String, device: IDevice) {
        val command =
            "am start $packageName/$mainActivity -c android.intent.category.LAUNCHER -a android.intent.action.MAIN"
        device.executeShellCommand(
            command,
            ShellOutputReceiver()
        )
    }

    /**
     * Stops recoding.
     */
    fun stopRecording() {
        shouldRun = false
        client?.stopSamplingProfiler()
    }

    /**
     * Force disconnect adb.
     */
    fun forceDisconnect() {
        adbWrapper?.let {
            it.stop()
        }
    }

    @Throws(DeviceTimeoutException::class)
    private fun waitForDevice(adb: AdbWrapper) {
        var count = 0
        while (!adb.hasInitialDeviceList() && shouldRun) {
            try {
                Thread.sleep(100)
                count++
            } catch (ignored: InterruptedException) {
            }
            if (count > 100) {
                adb.stop()
                throw DeviceTimeoutException()
            }
        }
    }

    @Throws(AppTimeoutException::class)
    private fun waitForApplication(adb: AdbWrapper, device: IDevice, packageName: String) {
        var count = 0
        while (device.getClient(packageName) == null && shouldRun) {
            try {
                Thread.sleep(100)
                count++
            } catch (ignored: InterruptedException) {
            }
            if (count > 100) {
                adb.stop()
                throw AppTimeoutException(packageName)
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
}