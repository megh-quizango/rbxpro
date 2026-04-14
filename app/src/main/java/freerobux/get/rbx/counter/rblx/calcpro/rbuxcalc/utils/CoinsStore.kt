package freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc.utils

import android.content.Context

object CoinsStore {
    private const val PREFS = "rbx_coins"
    private const val KEY = "coins"

    fun get(context: Context): Int {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY, 0)
    }

    fun set(context: Context, value: Int) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY, value.coerceAtLeast(0))
            .apply()
    }

    fun add(context: Context, delta: Int): Int {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val next = (prefs.getInt(KEY, 0) + delta).coerceAtLeast(0)
        prefs.edit().putInt(KEY, next).apply()
        return next
    }
}

