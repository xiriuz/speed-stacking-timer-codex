package com.xiriuz.speedstackingtimer.data

import android.content.Context
import com.xiriuz.speedstackingtimer.core.BestRecordStore

class SharedPreferencesBestRecordStore(context: Context) : BestRecordStore {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override fun load(): Long? = if (preferences.contains(KEY_BEST_MILLIS)) {
        preferences.getLong(KEY_BEST_MILLIS, 0L)
    } else {
        null
    }

    override fun save(milliseconds: Long) {
        preferences.edit().putLong(KEY_BEST_MILLIS, milliseconds).apply()
    }

    override fun clear() {
        preferences.edit().remove(KEY_BEST_MILLIS).apply()
    }

    private companion object {
        const val PREFERENCES_NAME = "speed_stacking_timer"
        const val KEY_BEST_MILLIS = "best_millis"
    }
}
