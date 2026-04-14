package freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc.home

import freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc.R

object HomeDataProvider {

    fun getItems(): List<HomeItem> {
        return listOf(
            HomeItem.Header(coins = 0),

            // Grid 1
            HomeItem.Card("Dollar To Rbx", R.drawable.dr_to_rb, HomeAction.DollarToRbx),
            HomeItem.Card("Rbx To Dollar", R.drawable.rbx_to_dollar, HomeAction.RbxToDollar),

            // Grid 2
            HomeItem.Card("Play Game", R.drawable.gameplay, HomeAction.ChromeOnly),
            HomeItem.Card("BC To Rbx", R.drawable.bctorbx, HomeAction.BCToRbx),

            // Banner 1
            HomeItem.Banner(
                R.drawable.ad_ic4,
                R.drawable.banner,
                "Learn How to Get Robx",
                "Find strategies to grow your Robx balance"
            ),

            // Grid 3
            HomeItem.Card("TBC To Rbx", R.drawable.tbc_to_rbx, HomeAction.TBCToRbx),
            HomeItem.Card("Play Game", R.drawable.gameplay, HomeAction.ChromeOnly),

            // Grid 4
            HomeItem.Card("Scratch Card", R.drawable.scratch, HomeAction.ChromeOnly),
            HomeItem.Card("OBC To Rbx", R.drawable.obc_to_rbx, HomeAction.OBCToRbx),

            // Banner 2
            HomeItem.Banner(
                R.drawable.ad_ic3,
                R.drawable.banner1,
                "Robx Guide for Gamers",
                "Discover efficient strategies"
            ),

            // Grid 5
            HomeItem.Card("Play Game", R.drawable.gameplay, HomeAction.ChromeOnly),
            HomeItem.Card("Quiz Time", R.drawable.quiz_time, HomeAction.QuizTime),

            // Grid 6
            HomeItem.Card("Lucky Spin Wheels", R.drawable.spins, HomeAction.ChromeOnly),
            HomeItem.Card("Play Game", R.drawable.gameplay, HomeAction.ChromeOnly),

            // Banner 3
            HomeItem.Banner(
                R.drawable.ad_ic2,
                R.drawable.banner2,
                "Mastering Robx Earning",
                "Step-by-step tips"
            ),

            // Grid 7
            HomeItem.Card("Tips & Tricks", R.drawable.trick, HomeAction.ChromeOnly),
            HomeItem.Card("Meme", R.drawable.jokes, HomeAction.ChromeOnly),
        )
    }
}
