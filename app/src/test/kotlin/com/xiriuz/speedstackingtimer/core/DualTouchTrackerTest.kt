package com.xiriuz.speedstackingtimer.core

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DualTouchTrackerTest {
    private val tracker = DualTouchTracker()

    @Test
    fun `requires a pointer on each hand pad`() {
        tracker.updatePointer(1, HandPad.LEFT)
        assertFalse(tracker.bothPressed)

        tracker.updatePointer(2, HandPad.RIGHT)
        assertTrue(tracker.bothPressed)
    }

    @Test
    fun `releasing either hand makes both pressed false`() {
        tracker.updatePointer(1, HandPad.LEFT)
        tracker.updatePointer(2, HandPad.RIGHT)
        tracker.removePointer(1)

        assertFalse(tracker.bothPressed)
    }

    @Test
    fun `moving a pointer out of a pad releases that pad`() {
        tracker.updatePointer(1, HandPad.LEFT)
        tracker.updatePointer(2, HandPad.RIGHT)
        tracker.updatePointer(2, null)

        assertFalse(tracker.bothPressed)
    }

    @Test
    fun `cancel clears every pointer`() {
        tracker.updatePointer(1, HandPad.LEFT)
        tracker.updatePointer(2, HandPad.RIGHT)
        tracker.clear()

        assertFalse(tracker.leftPressed)
        assertFalse(tracker.rightPressed)
    }
}

