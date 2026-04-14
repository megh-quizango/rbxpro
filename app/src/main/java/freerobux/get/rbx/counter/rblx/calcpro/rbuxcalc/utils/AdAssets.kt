package freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc.utils

import freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc.R

object AdAssets {

    data class BannerSpec(
        val iconRes: Int,
        val imageRes: Int,
        val title: String,
        val subtitle: String
    )

    fun pickTopAdIconRes(): Int {
        val icons = listOf(
            R.drawable.ad_ic1,
            R.drawable.ad_ic2,
            R.drawable.ad_ic3,
            R.drawable.ad_ic4,
            R.drawable.ad_ic5,
            R.drawable.ad_ic6
        )
        return icons.random()
    }

    fun pickBannerSpec(): BannerSpec {
        val banners = listOf(
            BannerSpec(
                iconRes = R.drawable.ad_ic4,
                imageRes = R.drawable.banner,
                title = "Learn How to Get Robx",
                subtitle = "Find strategies to grow your Robx balance"
            ),
            BannerSpec(
                iconRes = R.drawable.ad_ic3,
                imageRes = R.drawable.banner1,
                title = "Robx Guide for Gamers",
                subtitle = "Master techniques for earning Robx and enjoy the..."
            ),
            BannerSpec(
                iconRes = R.drawable.ad_ic2,
                imageRes = R.drawable.banner2,
                title = "Mastering Robx Earning",
                subtitle = "Explore tips and strategies to earn Robx..."
            )
        )
        return banners.random()
    }
}

