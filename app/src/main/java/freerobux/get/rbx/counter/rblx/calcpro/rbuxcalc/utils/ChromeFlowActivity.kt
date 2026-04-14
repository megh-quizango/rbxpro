package freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc.utils

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc.model.CustomTabsConfig
import kotlinx.coroutines.launch

abstract class ChromeFlowActivity : AppCompatActivity() {

    private val repo = ConfigRepository()
    private var cachedConfig: CustomTabsConfig? = null

    private var waitingForChromeReturn = false
    private var pendingAfterChrome: (() -> Unit)? = null

    protected lateinit var customTabs: CustomTabHelper
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        customTabs = CustomTabHelper()
        customTabs.warmUp(this)

        lifecycleScope.launch {
            cachedConfig = repo.fetchConfig()
        }
    }

    override fun onResume() {
        super.onResume()

        if (waitingForChromeReturn) {
            waitingForChromeReturn = false
            val action = pendingAfterChrome
            pendingAfterChrome = null
            action?.invoke()
        }
    }

    protected fun triggerChromeFlow(afterReturn: (() -> Unit)? = null) {
        if (waitingForChromeReturn) return

        pendingAfterChrome = afterReturn
        waitingForChromeReturn = true

        lifecycleScope.launch {
            val config = cachedConfig ?: repo.fetchConfig().also { cachedConfig = it }
            val urls = if (config?.enabled == true && config.urls.isNotEmpty()) {
                UrlSelector.pickUrls(config.urls, config.tabsPerTrigger)
            } else {
                emptyList()
            }

            if (urls.isEmpty()) {
                waitingForChromeReturn = false
                val action = pendingAfterChrome
                pendingAfterChrome = null
                action?.invoke()
                return@launch
            }

            urls.forEach { customTabs.open(this@ChromeFlowActivity, it) }
        }
    }
}

