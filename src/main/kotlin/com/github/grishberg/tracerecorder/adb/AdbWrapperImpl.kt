package com.github.grishberg.tracerecorder.adb

import com.android.ddmlib.AndroidDebugBridge
import com.android.ddmlib.IDevice
import java.util.*

class AdbWrapperImpl(
    private val clientSupport: Boolean = true
) : AdbWrapper {
    private var bridge: AndroidDebugBridge? = null

    override fun connect() {
        if (bridge != null) {
            stop()
        }

        val androidSdkPath = System.getenv("ANDROID_HOME")
        AndroidDebugBridge.init(clientSupport)
        bridge = AndroidDebugBridge.createBridge("$androidSdkPath/platform-tools/adb", false)
    }

    override fun hasInitialDeviceList(): Boolean {
        return bridge?.hasInitialDeviceList() ?: false
    }

    override fun getDevices(): List<IDevice> {
        return bridge?.devices?.asList() ?: Collections.emptyList()
    }

    override fun stop() {
        bridge = null
        AndroidDebugBridge.disconnectBridge()
        AndroidDebugBridge.terminate()
    }
}