package com.github.grishberg.tracerecorder

data class SystraceRecordResult(
    val records: List<SystraceRecord>,
    val startOffset: Double,
    val parentTs: Double
)

data class SystraceRecord(
    val name: String,
    val cpu: String,
    val startTime: Double, // System.upTimeInMs
    var endTime: Double = 0.0 // System.upTimeInMs
)
