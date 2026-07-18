package com.xiriuz.speedstackingtimer.core

fun interface NanoClock {
    fun nowNanos(): Long
}

object SystemNanoClock : NanoClock {
    override fun nowNanos(): Long = System.nanoTime()
}

