package com.github.grishberg.tracerecorder.adb

import com.android.ddmlib.IDevice

interface AdbWrapper {
    fun hasInitialDeviceList(): Boolean
    fun getDevices(): Array<IDevice>
    fun stop()
}