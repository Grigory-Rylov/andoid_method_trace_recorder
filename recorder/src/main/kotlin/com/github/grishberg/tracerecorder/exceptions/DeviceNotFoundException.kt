package com.github.grishberg.tracerecorder.exceptions

import com.github.grishberg.tracerecorder.SerialNumber

/**
 * Thrown when device with the given serial number is not found.
 */
internal class DeviceNotFoundException(serialNumber: SerialNumber) :
    MethodTraceRecordException("Device $serialNumber not found.")
