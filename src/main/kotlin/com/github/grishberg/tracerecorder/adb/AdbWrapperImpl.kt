package com.github.grishberg.tracerecorder.adb

import com.android.ddmlib.AndroidDebugBridge
import com.android.ddmlib.IDevice

class AdbWrapperImpl(
    clientSupport: Boolean = true
) : AdbWrapper {
    private val bridge: AndroidDebugBridge

    init {
        val androidSdkPath = System.getenv("ANDROID_HOME")
        AndroidDebugBridge.init(clientSupport)
        bridge = AndroidDebugBridge.createBridge("$androidSdkPath/platform-tools/adb", false)
    }

    override fun hasInitialDeviceList() = bridge.hasInitialDeviceList()

    override fun getDevices(): Array<IDevice> {
        return bridge.devices
    }

    override fun stop() {
        AndroidDebugBridge.disconnectBridge()
        AndroidDebugBridge.terminate()
    }
}