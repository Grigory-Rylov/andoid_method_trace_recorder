package com.android.ddmlib;

import com.github.grishberg.tracerecorder.adb.AdbWrapper;

public class MonitorThreadStopper {
    public static void stopMonitorThread() {
        MonitorThread.getInstance().interrupt();
    }
}
