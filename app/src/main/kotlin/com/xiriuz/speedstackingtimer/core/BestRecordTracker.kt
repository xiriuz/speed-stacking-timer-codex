package com.xiriuz.speedstackingtimer.core

interface BestRecordStore {
    fun load(): Long?
    fun save(milliseconds: Long)
    fun clear()
}

class BestRecordTracker(private val store: BestRecordStore) {
    var bestMillis: Long? = store.load()?.takeIf { it > 0L }
        private set

    fun record(milliseconds: Long) {
        if (milliseconds <= 0L) return
        val currentBest = bestMillis
        if (currentBest == null || milliseconds < currentBest) {
            bestMillis = milliseconds
            store.save(milliseconds)
        }
    }

    fun reset() {
        bestMillis = null
        store.clear()
    }
}

