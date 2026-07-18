package com.xiriuz.speedstackingtimer.presentation

import java.util.Locale

object TimerFormatter {
    fun format(elapsedMillis: Long): String {
        val safeMillis = elapsedMillis.coerceAtLeast(0L)
        val minutes = safeMillis / 60_000L
        val seconds = (safeMillis % 60_000L) / 1_000L
        val milliseconds = safeMillis % 1_000L

        return if (minutes == 0L) {
            String.format(Locale.US, "%d.%03d", seconds, milliseconds)
        } else {
            String.format(Locale.US, "%d:%02d.%03d", minutes, seconds, milliseconds)
        }
    }
}

