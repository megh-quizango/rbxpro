package freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc.tips

import freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc.utils.AdAssets

sealed class TipsListItem {
    data class TipRow(val tip: Tip) : TipsListItem()
    data object PlayGameRow : TipsListItem()
    data class BannerRow(val banner: AdAssets.BannerSpec) : TipsListItem()
}

