package freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc

import android.app.Application
import com.onesignal.OneSignal

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        initOneSignal()
    }

    private fun initOneSignal() {
        try {

            OneSignal.initWithContext(this,"")

        } catch (e: Exception) {
            e.printStackTrace() // prevents crash
        }
    }
}