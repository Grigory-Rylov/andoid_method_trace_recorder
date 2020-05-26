package com.android.ddmlib;

import com.github.grishberg.tracerecorder.common.RecorderLogger;

public class MonitorThreadLoggerBridge {
    public static void setLogger(RecorderLogger logger) {
        MonitorThread.log = logger;
    }
}
