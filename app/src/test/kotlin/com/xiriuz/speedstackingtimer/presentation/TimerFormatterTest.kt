package com.xiriuz.speedstackingtimer.presentation

import org.junit.Assert.assertEquals
import org.junit.Test

class TimerFormatterTest {
    @Test
    fun `formats milliseconds below one minute`() {
        assertEquals("0.000", TimerFormatter.format(0))
        assertEquals("12.345", TimerFormatter.format(12_345))
    }

    @Test
    fun `formats minutes without losing millisecond precision`() {
        assertEquals("1:01.342", TimerFormatter.format(61_342))
    }
}
