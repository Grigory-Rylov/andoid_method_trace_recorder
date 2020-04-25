package com.github.grishberg.tracerecorder.adb

import com.github.grishberg.tracerecorder.MethodTraceEventListener
import com.github.grishberg.tracerecorder.common.RecorderLogger
import com.github.grishberg.tracerecorder.exceptions.StartActivityException
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import junit.framework.Assert.assertEquals
import org.junit.Test

internal class ShellOutputReceiverTest {

    val errorOutput = arrayOf(
        "Starting: Intent { act=android.intent.action.MAIN cat=[android.intent.category.LAUNCHER] cmp=com.grishberg.yayp.debug/.presentation.MainActivity }",
        " Error type 3",
        " Error: Activity class {com.grishberg.yayp.debug/com.grishberg.yayp.debug.presentation.MainActivity} does not exist.",
        " "
    )
    val logger = mock<RecorderLogger>()
    val listener = mock<MethodTraceEventListener>()
    val captor = argumentCaptor<StartActivityException>()
    val underTest = ShellOutputReceiver(logger, listener)

    @Test
    fun throwException() {
        underTest.processNewLines(errorOutput)
        verify(listener).fail(captor.capture())

        assertEquals(
            "Activity class {com.grishberg.yayp.debug/com.grishberg.yayp.debug.presentation.MainActivity} does not exist.",
            captor.firstValue.message
        )
    }
}