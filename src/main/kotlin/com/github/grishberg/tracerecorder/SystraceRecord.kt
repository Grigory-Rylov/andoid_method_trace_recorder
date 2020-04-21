package com.github.grishberg.tracerecorder

data class SystraceRecord(val name: String, val cpu: String, val startTime: Double, var endTime: Double = 0.0)