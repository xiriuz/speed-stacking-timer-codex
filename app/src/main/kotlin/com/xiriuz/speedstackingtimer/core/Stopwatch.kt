package com.xiriuz.speedstackingtimer.core

class Stopwatch(private val clock: NanoClock) {
    private var accumulatedNanos = 0L
    private var startedAtNanos: Long? = null

    val isRunning: Boolean
        get() = startedAtNanos != null

    fun start() {
        if (startedAtNanos == null) {
            startedAtNanos = clock.nowNanos()
        }
    }

    fun stop() {
        val startedAt = startedAtNanos ?: return
        accumulatedNanos += (clock.nowNanos() - startedAt).coerceAtLeast(0L)
        startedAtNanos = null
    }

    fun reset() {
        accumulatedNanos = 0L
        startedAtNanos = null
    }

    fun elapsedMillis(): Long {
        val currentRun = startedAtNanos?.let { startedAt ->
            (clock.nowNanos() - startedAt).coerceAtLeast(0L)
        } ?: 0L
        return (accumulatedNanos + currentRun) / NANOS_PER_MILLISECOND
    }

    private companion object {
        const val NANOS_PER_MILLISECOND = 1_000_000L
    }
}

