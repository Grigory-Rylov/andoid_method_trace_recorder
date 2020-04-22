package com.github.grishberg.tracerecorder.adb

import com.android.ddmlib.IDevice

interface AdbWrapper {
    fun connect()
    fun hasInitialDeviceList(): Boolean
    fun getDevices(): List<IDevice>
    fun stop()
}