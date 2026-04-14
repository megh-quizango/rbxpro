package freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc.utils

import android.content.Context
import androidx.startup.Initializer

class FirebaseInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        // Lightweight init only
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
