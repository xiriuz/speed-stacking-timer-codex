package com.xiriuz.speedstackingtimer.core

enum class TimerPhase {
    IDLE,
    HOLDING_GREEN,
    ARMED_RED,
    RUNNING,
    STOPPED,
}

data class TimerSnapshot(
    val phase: TimerPhase,
    val elapsedMillis: Long,
    val leftPressed: Boolean,
    val rightPressed: Boolean,
)

class StackingTimerController(
    private val clock: NanoClock,
    private val holdDurationMillis: Long = DEFAULT_HOLD_DURATION_MILLIS,
) {
    private val stopwatch = Stopwatch(clock)
    private var phase = TimerPhase.IDLE
    private var holdStartedAtNanos = 0L
    private var leftPressed = false
    private var rightPressed = false

    fun onHandsChanged(leftPressed: Boolean, rightPressed: Boolean) {
        this.leftPressed = leftPressed
        this.rightPressed = rightPressed
        val bothPressed = leftPressed && rightPressed
        val bothReleased = !leftPressed && !rightPressed

        when (phase) {
            TimerPhase.IDLE -> if (bothPressed) beginHolding()
            TimerPhase.HOLDING_GREEN -> {
                if (!bothPressed) {
                    phase = TimerPhase.IDLE
                } else {
                    armIfReady()
                }
            }
            TimerPhase.ARMED_RED -> if (bothReleased) {
                stopwatch.start()
                phase = TimerPhase.RUNNING
            }
            TimerPhase.RUNNING -> if (bothPressed) {
                stopwatch.stop()
                phase = TimerPhase.STOPPED
            }
            TimerPhase.STOPPED -> if (bothReleased) {
                phase = TimerPhase.IDLE
            }
        }
    }

    fun tick() {
        if (phase == TimerPhase.HOLDING_GREEN) {
            armIfReady()
        }
    }

    fun snapshot(): TimerSnapshot = TimerSnapshot(
        phase = phase,
        elapsedMillis = stopwatch.elapsedMillis(),
        leftPressed = leftPressed,
        rightPressed = rightPressed,
    )

    private fun beginHolding() {
        stopwatch.reset()
        holdStartedAtNanos = clock.nowNanos()
        phase = TimerPhase.HOLDING_GREEN
    }

    private fun armIfReady() {
        val heldNanos = clock.nowNanos() - holdStartedAtNanos
        if (heldNanos >= holdDurationMillis * NANOS_PER_MILLISECOND) {
            phase = TimerPhase.ARMED_RED
        }
    }

    private companion object {
        const val DEFAULT_HOLD_DURATION_MILLIS = 700L
        const val NANOS_PER_MILLISECOND = 1_000_000L
    }
}
