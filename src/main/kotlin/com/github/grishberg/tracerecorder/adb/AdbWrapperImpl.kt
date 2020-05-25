package com.github.grishberg.tracerecorder.adb

import com.android.ddmlib.AndroidDebugBridge
import com.android.ddmlib.IDevice
import com.github.grishberg.tracerecorder.common.RecorderLogger
import java.util.*

private const val TAG = "AdbWrapperImpl"

class AdbWrapperImpl(
    private val clientSupport: Boolean = true,
    private val logger: RecorderLogger,
    private val forceNewBridge: Boolean = false
) : AdbWrapper {
    private var bridge: AndroidDebugBridge? = null
    private val androidSdkPath = System.getenv("ANDROID_HOME")

    override fun connect() {
        logger.d("$TAG: connect, bridge=$bridge")
        if (bridge != null) {
            stop()
        }

        logger.d("$TAG: init with clientSupport=$clientSupport")
        AndroidDebugBridge.init(clientSupport)

        logger.d("$TAG: creating ADB bridge")
        bridge = AndroidDebugBridge.createBridge("$androidSdkPath/platform-tools/adb", forceNewBridge)
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