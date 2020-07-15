package com.github.grishberg.tracerecorder.adb

import com.android.ddmlib.AndroidDebugBridge
import com.android.ddmlib.IDevice
import com.github.grishberg.tracerecorder.common.RecorderLogger
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.*

private const val TAG = "AdbWrapperImpl"

class AdbWrapperImpl(
    private val clientSupport: Boolean = true,
    private val logger: RecorderLogger,
    androidHome: String? = null,
    private val forceNewBridge: Boolean = false
) : AdbWrapper {
    private var bridge: AndroidDebugBridge? = null
    private val androidSdkPath: String? = androidHome ?: System.getenv("ANDROID_HOME")

    override fun connect() {
        logger.d("$TAG: connect, bridge=$bridge")
        if (bridge != null) {
            stop()
        }

        AndroidDebugBridge.initIfNeeded(clientSupport)

        logger.d("$TAG: creating ADB bridge with android_home=$androidSdkPath")
        if (androidSdkPath != null) {
            val adbPath = File(androidSdkPath, "platform-tools/adb").absolutePath
            bridge = AndroidDebugBridge.createBridge(adbPath, forceNewBridge)
        } else {
            bridge = AndroidDebugBridge.createBridge()
        }
        logger.d("$TAG: connected, bridge=$bridge")
    }

    override fun connect(remote: String) {
        adbWificonnect(remote)
        connect()
    }

    override fun isConnected(): Boolean {
        return bridge?.isConnected ?: false
    }

    override fun hasInitialDeviceList(): Boolean {
        return bridge?.hasInitialDeviceList() ?: false
    }

    override fun getDevices(): List<IDevice> {
        return bridge?.devices?.asList() ?: Collections.emptyList()
    }

    override fun stop() {
        logger.d("$TAG: stop")
        bridge = null
        AndroidDebugBridge.disconnectBridge()
        AndroidDebugBridge.terminate()
    }

    private fun adbWificonnect(ipAddress: String): Boolean {
        var connected = false
        logger.d("$TAG: connect to $ipAddress...")

        val process =
            Runtime.getRuntime().exec(File(androidSdkPath, "platform-tools/adb").absolutePath + " connect " + ipAddress)
        val inBuffer = BufferedReader(InputStreamReader(process.inputStream))
        var line: String?
        var message: String? = null
        while (inBuffer.readLine().also { line = it } != null) {
            if (line!!.contains("connected")) {
                connected = true
            }
            message = line
        }
        if (connected) {
            logger.d("$TAG: $message")
            return true
        }

        return false
    }
}