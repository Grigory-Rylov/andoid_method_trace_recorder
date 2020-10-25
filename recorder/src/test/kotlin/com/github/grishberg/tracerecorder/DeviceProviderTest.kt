package com.github.grishberg.tracerecorder

import com.github.grishberg.android.adb.AdbWrapper
import com.github.grishberg.android.adb.ConnectedDeviceWrapper
import com.github.grishberg.tracerecorder.common.NoOpLogger
import com.github.grishberg.tracerecorder.exceptions.DeviceNotFoundException
import com.github.grishberg.tracerecorder.exceptions.NoDeviceException
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert
import org.junit.Test

internal class DeviceProviderTest {

    private val adb = mock<AdbWrapper>()

    @Test
    fun `device by serial`() {
        val underTest =
            createUnderTest(DeviceProvider.ConnectStrategy.Serial(SerialNumber(TEST_SERIAL_2)))
        whenDevicesConnected()

        Assert.assertEquals(TEST_DEVICE_2, underTest.device)
    }

    @Test(expected = DeviceNotFoundException::class)
    fun `device by serial not found`() {
        val underTest =
            createUnderTest(DeviceProvider.ConnectStrategy.Serial(SerialNumber(TEST_SERIAL_NOT_FOUND)))
        whenDevicesConnected()

        underTest.device
    }

    @Test(expected = NoDeviceException::class)
    fun `device by serial when no devices connected`() {
        val underTest =
            createUnderTest(DeviceProvider.ConnectStrategy.Serial(SerialNumber(TEST_SERIAL_NOT_FOUND)))
        whenNoDevicesConnected()

        underTest.device
    }

    @Test
    fun `first device`() {
        val underTest = createUnderTest(DeviceProvider.ConnectStrategy.First)
        whenDevicesConnected()

        Assert.assertEquals(TEST_DEVICE_1, underTest.device)
    }

    @Test(expected = NoDeviceException::class)
    fun `first device when no devices connected`() {
        val underTest = createUnderTest(DeviceProvider.ConnectStrategy.First)
        whenNoDevicesConnected()

        underTest.device
    }

    private fun whenDevicesConnected() {
        whenever(adb.deviceList()).doReturn(CONNECTED_DEVICES)
    }

    private fun whenNoDevicesConnected() {
        whenever(adb.deviceList()).doReturn(emptyList())
    }

    private fun createUnderTest(connectStrategy: DeviceProvider.ConnectStrategy): DeviceProvider {
        return DeviceProvider(adb, NoOpLogger(), connectStrategy)
    }

    private companion object {
        const val TEST_SERIAL_NOT_FOUND = "serial"
        const val TEST_SERIAL_1 = "1"
        const val TEST_SERIAL_2 = "2"
        val TEST_DEVICE_1 = makeTestDevice(TEST_SERIAL_1)
        val TEST_DEVICE_2 = makeTestDevice(TEST_SERIAL_2)

        val CONNECTED_DEVICES = listOf(TEST_DEVICE_1, TEST_DEVICE_2)

        private fun makeTestDevice(serial: String) = mock<ConnectedDeviceWrapper> {
            on { serialNumber } doReturn serial
        }
    }
}
