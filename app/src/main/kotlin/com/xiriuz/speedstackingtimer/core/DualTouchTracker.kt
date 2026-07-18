package com.xiriuz.speedstackingtimer.core

enum class HandPad {
    LEFT,
    RIGHT,
}

class DualTouchTracker {
    private val pointerPads = mutableMapOf<Long, HandPad>()

    val leftPressed: Boolean
        get() = pointerPads.containsValue(HandPad.LEFT)

    val rightPressed: Boolean
        get() = pointerPads.containsValue(HandPad.RIGHT)

    val bothPressed: Boolean
        get() = leftPressed && rightPressed

    fun updatePointer(pointerId: Long, handPad: HandPad?) {
        if (handPad == null) {
            pointerPads.remove(pointerId)
        } else {
            pointerPads[pointerId] = handPad
        }
    }

    fun removePointer(pointerId: Long) {
        pointerPads.remove(pointerId)
    }

    fun clear() {
        pointerPads.clear()
    }
}

