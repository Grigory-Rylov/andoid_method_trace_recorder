package com.github.grishberg.tracerecorder

import com.android.ddmlib.IDevice
import com.github.grishberg.tracerecorder.DeviceProvider.ConnectStrategy
import com.github.grishberg.tracerecorder.adb.AdbWrapper
import com.github.grishberg.tracerecorder.common.RecorderLogger
import com.github.grishberg.tracerecorder.exceptions.DeviceNotFoundException
import com.github.grishberg.tracerecorder.exceptions.NoDeviceException

private const val TAG = "DeviceProvider"

inline class SerialNumber(val value: String)

/**
 * Provider of [IDevice].
 *
 * @param [adb][AdbWrapper] to fetch connected devices from.
 * @param [logger][RecorderLogger] to log operations.
 * @param [connectStrategy][ConnectStrategy] a strategy to obtain connected device.
 */
internal class DeviceProvider(
    private val adb: AdbWrapper,
    private val logger: RecorderLogger,
    private val connectStrategy: ConnectStrategy = ConnectStrategy.First
) {

    /**
     * Obtains connected [IDevice], according to the [connectStrategy].
     *
     * @return connected [IDevice].
     * @throws NoDeviceException if no devices connected.
     * @throws DeviceNotFoundException if device with given serial number is not found.
     */
    val device: IDevice
        get() = connectStrategy.device(fetchDevices())

    private fun ConnectStrategy.device(devices: List<IDevice>): IDevice {
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

    private fun fetchDevices(): List<IDevice> {
        logger.d("$TAG: fetching devices")
        val devices = adb.getDevices()
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
