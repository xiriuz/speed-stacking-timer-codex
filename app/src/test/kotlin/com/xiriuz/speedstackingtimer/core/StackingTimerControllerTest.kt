package com.xiriuz.speedstackingtimer.core

import org.junit.Assert.assertEquals
import org.junit.Test

class StackingTimerControllerTest {
    private val clock = FakeNanoClock()
    private val controller = StackingTimerController(clock, holdDurationMillis = 500)

    @Test
    fun `both hands turn the timer green while holding`() {
        controller.onHandsChanged(leftPressed = true, rightPressed = true)

        assertEquals(TimerPhase.HOLDING_GREEN, controller.snapshot().phase)
    }

    @Test
    fun `holding both hands long enough arms the red light`() {
        controller.onHandsChanged(true, true)
        clock.advanceMillis(500)
        controller.tick()

        assertEquals(TimerPhase.ARMED_RED, controller.snapshot().phase)
    }

    @Test
    fun `releasing both hands after red starts the timer`() {
        armTimer()
        controller.onHandsChanged(false, false)
        clock.advanceMillis(321)

        assertEquals(TimerPhase.RUNNING, controller.snapshot().phase)
        assertEquals(321, controller.snapshot().elapsedMillis)
    }

    @Test
    fun `touching both pads again stops the running timer`() {
        armTimer()
        controller.onHandsChanged(false, false)
        clock.advanceMillis(987)
        controller.onHandsChanged(true, true)
        clock.advanceMillis(100)

        assertEquals(TimerPhase.STOPPED, controller.snapshot().phase)
        assertEquals(987, controller.snapshot().elapsedMillis)
    }

    @Test
    fun `early release cancels preparation`() {
        controller.onHandsChanged(true, true)
        clock.advanceMillis(499)
        controller.onHandsChanged(false, false)

        assertEquals(TimerPhase.IDLE, controller.snapshot().phase)
    }

    @Test
    fun `one hand cannot start or stop the timer`() {
        controller.onHandsChanged(true, false)
        assertEquals(TimerPhase.IDLE, controller.snapshot().phase)

        armTimer()
        controller.onHandsChanged(false, false)
        controller.onHandsChanged(true, false)

        assertEquals(TimerPhase.RUNNING, controller.snapshot().phase)
    }

    @Test
    fun `releasing after stop returns to idle while retaining the result`() {
        armTimer()
        controller.onHandsChanged(false, false)
        clock.advanceMillis(700)
        controller.onHandsChanged(true, true)
        controller.onHandsChanged(false, false)

        assertEquals(TimerPhase.IDLE, controller.snapshot().phase)
        assertEquals(700, controller.snapshot().elapsedMillis)
    }

    private fun armTimer() {
        controller.onHandsChanged(true, true)
        clock.advanceMillis(500)
        controller.tick()
    }
}
