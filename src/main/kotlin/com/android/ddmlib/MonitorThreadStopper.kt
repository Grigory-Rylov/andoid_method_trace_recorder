package com.android.ddmlib

object MonitorThreadStopper {
    fun stopMonitorThread() {
        MonitorThread.getInstance().interrupt()
    }
}