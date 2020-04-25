package com.github.grishberg.tracerecorder.adb

import com.android.ddmlib.AndroidDebugBridge
import com.android.ddmlib.IDevice
import com.github.grishberg.tracerecorder.common.RecorderLogger
import java.util.*

private const val TAG = "AdbWrapperImpl"

class AdbWrapperImpl(
    private val clientSupport: Boolean = true,
    private val logger: RecorderLogger
) : AdbWrapper {
    private var bridge: AndroidDebugBridge? = null

    override fun connect() {
        logger.d("$TAG: connect, bridge=$bridge")
        if (bridge != null) {
            stop()
        }

        val androidSdkPath = System.getenv("ANDROID_HOME")
        logger.d("$TAG: init with clientSupport=$clientSupport")
        AndroidDebugBridge.init(clientSupport)

        logger.d("$TAG: creating ADB bridge")
        bridge = AndroidDebugBridge.createBridge("$androidSdkPath/platform-tools/adb", false)
        logger.d("$TAG: connected, bridge=$bridge")
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
}