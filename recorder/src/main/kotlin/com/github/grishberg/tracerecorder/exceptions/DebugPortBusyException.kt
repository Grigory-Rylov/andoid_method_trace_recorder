package com.github.grishberg.tracerecorder.exceptions

class DebugPortBusyException(port: Int) : MethodTraceRecordException(
    "Port $port is busy, close program used port.\n" +
            "Use \"lsof -nP +c 15 | grep '$port (LISTEN)'\" to find who used port."
)