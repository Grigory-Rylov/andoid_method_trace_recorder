package com.github.grishberg.tracerecorder.exceptions

class AppTimeoutException(appPackage: String) : MethodTraceRecordException("$appPackage not started")