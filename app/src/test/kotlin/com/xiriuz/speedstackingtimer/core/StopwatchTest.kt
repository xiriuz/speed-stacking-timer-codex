package com.xiriuz.speedstackingtimer.core

import org.junit.Assert.assertEquals
import org.junit.Test

class StopwatchTest {
    private val clock = FakeNanoClock()
    private val stopwatch = Stopwatch(clock)

    @Test
    fun `measures elapsed time with a monotonic clock`() {
        stopwatch.start()
        clock.advanceMillis(1_234)

        assertEquals(1_234, stopwatch.elapsedMillis())
    }

    @Test
    fun `freezes elapsed time when stopped`() {
        stopwatch.start()
        clock.advanceMillis(800)
        stopwatch.stop()
        clock.advanceMillis(500)

        assertEquals(800, stopwatch.elapsedMillis())
    }

    @Test
    fun `reset clears the previous measurement`() {
        stopwatch.start()
        clock.advanceMillis(500)
        stopwatch.stop()
        stopwatch.reset()

        assertEquals(0, stopwatch.elapsedMillis())
    }
}

class FakeNanoClock(var nowNanos: Long = 0L) : NanoClock {
    override fun nowNanos(): Long = nowNanos

    fun advanceMillis(milliseconds: Long) {
        nowNanos += milliseconds * 1_000_000L
    }
}

