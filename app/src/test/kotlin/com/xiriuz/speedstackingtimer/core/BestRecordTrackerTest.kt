package com.xiriuz.speedstackingtimer.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BestRecordTrackerTest {
    private val store = InMemoryBestRecordStore()
    private val tracker = BestRecordTracker(store)

    @Test
    fun `first completed time becomes the best record`() {
        tracker.record(1_234)

        assertEquals(1_234L, tracker.bestMillis)
        assertEquals(1_234L, store.value)
    }

    @Test
    fun `only a faster completed time replaces the best record`() {
        tracker.record(1_234)
        tracker.record(1_500)
        tracker.record(999)

        assertEquals(999L, tracker.bestMillis)
    }

    @Test
    fun `zero duration is not a valid record`() {
        tracker.record(0)

        assertNull(tracker.bestMillis)
    }

    @Test
    fun `saved record is restored when tracker is recreated`() {
        store.value = 2_345

        assertEquals(2_345L, BestRecordTracker(store).bestMillis)
    }

    @Test
    fun `reset clears memory and persistent storage`() {
        tracker.record(1_234)

        tracker.reset()

        assertNull(tracker.bestMillis)
        assertNull(store.value)
    }
}

private class InMemoryBestRecordStore : BestRecordStore {
    var value: Long? = null

    override fun load(): Long? = value

    override fun save(milliseconds: Long) {
        value = milliseconds
    }

    override fun clear() {
        value = null
    }
}
