package freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc.utils

import android.content.Context

class UrlCache(context: Context) {

    private val prefs = context.getSharedPreferences("app_cache", Context.MODE_PRIVATE)

    fun saveUrl(url: String) {
        prefs.edit().putString("cached_url", url).apply()
    }

    fun getUrl(): String? {
        return prefs.getString("cached_url", null)
    }
}
