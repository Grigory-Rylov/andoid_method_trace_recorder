package com.github.grishberg.tracerecorder.common

import com.github.grishberg.android.adb.AdbLogger

class NoOpLogger : AdbLogger {
    override fun d(msg: String) = Unit

    override fun e(s: String) = Unit

    override fun e(msg: String, t: Throwable) = Unit

    override fun w(msg: String) = Unit
}
