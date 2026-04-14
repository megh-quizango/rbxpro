package freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc.meme

import freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc.utils.AdAssets

sealed class MemeListItem {
    data class MemeRow(val meme: Meme) : MemeListItem()
    data class BannerRow(val banner: AdAssets.BannerSpec) : MemeListItem()
}

