package com.github.grishberg.tracerecorder

import com.github.grishberg.android.adb.AdbLogger
import com.github.grishberg.android.adb.AdbWrapper
import com.github.grishberg.android.adb.ConnectedDeviceWrapper
import com.github.grishberg.tracerecorder.DeviceProviderImpl.ConnectStrategy
import com.github.grishberg.tracerecorder.exceptions.DeviceNotFoundException
import com.github.grishberg.tracerecorder.exceptions.MethodTraceRecordException
import com.github.grishberg.tracerecorder.exceptions.NoDeviceException

private const val TAG = "DeviceProvider"

inline class SerialNumber(val value: String)

interface DeviceProvider {
    /**
     * Obtains connected [ConnectedDeviceWrapper], according to the [connectStrategy].
     *
     * @return connected [ConnectedDeviceWrapper].
     * @throws NoDeviceException if no devices connected.
     * @throws DeviceNotFoundException if device with given serial number is not found.
     */
    @Throws(MethodTraceRecordException::class)
    fun getDevice(): ConnectedDeviceWrapper
}

/**
 * Provider of [ConnectedDeviceWrapper].
 *
 * @param [adb][AdbWrapper] to fetch connected devices from.
 * @param [logger][AdbLogger] to log operations.
 * @param [connectStrategy][ConnectStrategy] a strategy to obtain connected device.
 */
class DeviceProviderImpl(
    private val adb: AdbWrapper,
    private val logger: AdbLogger,
    private val connectStrategy: ConnectStrategy = ConnectStrategy.First
) : DeviceProvider {
    override fun getDevice(): ConnectedDeviceWrapper = connectStrategy.device(fetchDevices())

    private fun ConnectStrategy.device(devices: List<ConnectedDeviceWrapper>): ConnectedDeviceWrapper {
        return when (this) {
            ConnectStrategy.First -> {
                logger.d("$TAG: first device")
                devices.first()
            }

            is ConnectStrategy.Serial -> {
                logger.d("$TAG: device by $serialNumber")
                devices.find { it.serialNumber == serialNumber.value }
                    ?: throw DeviceNotFoundException(serialNumber)
            }
        }
    }

    private fun fetchDevices(): List<ConnectedDeviceWrapper> {
        logger.d("$TAG: fetching devices")
        val devices = adb.deviceList()
        logger.d("$TAG: found devices: $devices")
        if (devices.isEmpty()) {
            throw NoDeviceException()
        }
        return devices
    }

    /**
     * A strategy to obtain connected device.
     */
    sealed class ConnectStrategy {

        /**
         * A strategy to connect to the first connected device.
         */
        object First : ConnectStrategy()

        /**
         * A strategy to connect to the device with the given [serialNumber].
         */
        data class Serial(val serialNumber: SerialNumber) : ConnectStrategy()

        companion object {

            /**
             * Creates a [ConnectStrategy] based on the given [serialNumber].
             */
            fun create(serialNumber: SerialNumber?): ConnectStrategy {
                return serialNumber?.let(::Serial) ?: First
            }
        }
    }
}
