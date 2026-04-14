package freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc.model

data class UrlItem(
    val url: String = "",
    val weight: Int = 0
)

data class CustomTabsConfig(
    val enabled: Boolean = false,
    val tabsPerTrigger: Int = 1,
    val urls: List<UrlItem> = emptyList()
)
